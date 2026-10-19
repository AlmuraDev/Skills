/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.inspirenxe.skills.impl.content.type.skill.builtin;

import com.almuradev.toolbox.inject.event.Witness;
import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillHolderContainer;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.impl.SkillImpl;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.Furnace;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.tileentity.SmeltEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.slot.FuelSlot;
import org.spongepowered.api.item.inventory.slot.InputSlot;
import org.spongepowered.api.item.inventory.slot.OutputSlot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

/**
 * Smelting XP: the player who puts items into a furnace's input slot is credited when those items finish smelting.
 *
 * <ul>
 *     <li>Every furnace keeps a queue of who loaded how many of the items in its input slot, oldest first. Each finished smelt
 *     ({@code SmeltEvent.Finish}) credits the head of the queue with the rewards of the {@code SmeltEvent.Finish} item chains for the
 *     result. Anything that fires {@code SmeltEvent} (the vanilla furnace and furnaces built on it) works.</li>
 *     <li>Only items players put in by hand count. Items fed by hoppers or pipes are not in the queue and earn nothing; who takes the
 *     result out doesn't matter.</li>
 *     <li>A queue never holds more than the input slot does: taking items back out trims it (newest first), swapping the stack for
 *     another item clears it, breaking the furnace drops it.</li>
 *     <li>A player who is offline (or has no loaded skills in the furnace's world) when an item finishes is paid when they are online
 *     again, at the levels they have then.</li>
 *     <li>Queues and unpaid rewards are saved to {@code smelting-data.json} in the config directory, so they survive restarts.</li>
 * </ul>
 */
@Singleton
public final class SmeltingTracker implements Witness {

    private static final String DATA_FILE = "smelting-data.json";
    private static final int DATA_VERSION = 1;
    private static final long MAINTENANCE_INTERVAL_SECONDS = 30;
    private static final long JOIN_PAYOUT_DELAY_TICKS = 100;

    private static final DataQuery VERSION = DataQuery.of("version");
    private static final DataQuery FURNACES = DataQuery.of("furnaces");
    private static final DataQuery PENDING = DataQuery.of("pending");
    private static final DataQuery WORLD = DataQuery.of("world");
    private static final DataQuery X = DataQuery.of("x");
    private static final DataQuery Y = DataQuery.of("y");
    private static final DataQuery Z = DataQuery.of("z");
    private static final DataQuery QUEUE = DataQuery.of("queue");
    private static final DataQuery PLAYER = DataQuery.of("player");
    private static final DataQuery CONTAINER = DataQuery.of("container");
    private static final DataQuery ITEM = DataQuery.of("item");
    private static final DataQuery COUNT = DataQuery.of("count");

    private final Logger logger;
    private final PluginContainer container;
    private final Scheduler scheduler;
    private final ServiceManager serviceManager;
    private final BuiltinEventListener listener;
    private final Path dataFile;

    // Furnace -> who loaded the items in its input slot, oldest first
    private final Map<FurnaceKey, Deque<Contribution>> queues = new HashMap<>();
    // Player -> furnace they last opened, used when a clicked input slot doesn't lead back to its furnace
    private final Map<UUID, FurnaceKey> openFurnaces = new HashMap<>();
    // Player -> rewards not paid yet (offline, or no loaded skills in the furnace's world)
    private final Map<UUID, List<PendingSmelt>> pending = new HashMap<>();

    private boolean dirty;
    private boolean checkedSlotTypes;

    @Inject
    public SmeltingTracker(final Logger logger, final PluginContainer container, final Scheduler scheduler, final ServiceManager serviceManager,
        final BuiltinEventListener listener, @ConfigDir(sharedRoot = false) final Path configDir) {
        this.logger = logger;
        this.container = container;
        this.scheduler = scheduler;
        this.serviceManager = serviceManager;
        this.listener = listener;
        this.dataFile = configDir.resolve(DATA_FILE);
    }

    @Listener
    public void onGameStartedServer(final GameStartedServerEvent event) {
        this.load();

        this.scheduler.createTaskBuilder()
            .name("Skills - Smelting maintenance")
            .delay(MAINTENANCE_INTERVAL_SECONDS, TimeUnit.SECONDS)
            .interval(MAINTENANCE_INTERVAL_SECONDS, TimeUnit.SECONDS)
            .execute(this::maintain)
            .submit(this.container);
    }

    @Listener
    public void onGameStoppingServer(final GameStoppingServerEvent event) {
        this.save();
    }

    @Listener
    public void onClientConnectionJoin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        if (!this.pending.containsKey(player.getUniqueId())) {
            return;
        }

        // Give the player's skills time to load from the database, maintenance retries if they aren't loaded yet
        final UUID playerId = player.getUniqueId();
        this.scheduler.createTaskBuilder()
            .delayTicks(JOIN_PAYOUT_DELAY_TICKS)
            .execute(() -> Sponge.getServer().getPlayer(playerId).ifPresent(this::payPending))
            .submit(this.container);
    }

    @Listener
    public void onClientConnectionDisconnect(final ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") final Player player) {
        this.openFurnaces.remove(player.getUniqueId());
    }

    @Listener(order = Order.POST)
    public void onInteractBlockSecondary(final InteractBlockEvent.Secondary event, @Root final Player player) {
        final TileEntity tile = event.getTargetBlock().getLocation().flatMap(Location::getTileEntity).orElse(null);
        if (tile instanceof Furnace) {
            this.openFurnaces.put(player.getUniqueId(), new FurnaceKey(tile.getLocation()));
        }
    }

    @Listener
    public void onInteractInventoryClose(final InteractInventoryEvent.Close event, @Root final Player player) {
        this.openFurnaces.remove(player.getUniqueId());
    }

    @Listener(order = Order.POST)
    public void onClickInventory(final ClickInventoryEvent event, @Root final Player player) {
        this.checkSlotTypes(event);

        final boolean canEarn = player.gameMode().get() != GameModes.CREATIVE;

        for (final SlotTransaction transaction : event.getTransactions()) {
            if (!transaction.isValid() || !isInputSlot(transaction.getSlot())) {
                continue;
            }

            FurnaceKey key = furnaceOf(transaction.getSlot());
            if (key == null) {
                key = this.openFurnaces.get(player.getUniqueId());
            }
            if (key == null) {
                continue;
            }

            final ItemStackSnapshot original = transaction.getOriginal();
            final ItemStackSnapshot result = transaction.getFinal();
            final boolean sameItem = original.isEmpty() || result.isEmpty() || isSameItem(original, result);

            if (!sameItem) {
                // Stack swapped for another item, everything queued for the old one is gone
                this.clearQueue(key);
            }

            final int added = result.isEmpty() ? 0 : sameItem ? result.getQuantity() - original.getQuantity() : result.getQuantity();
            if (added > 0 && canEarn) {
                this.enqueue(key, player.getUniqueId(), added);
            }

            // Taken out (or never tracked): the queue can't hold more than the slot does
            this.trimQueue(key, result.isEmpty() ? 0 : result.getQuantity());
        }
    }

    @Listener
    public void onSmeltFinish(final SmeltEvent.Finish event) {
        if (this.queues.isEmpty()) {
            return;
        }

        final Furnace furnace = event.getTargetTile();
        final FurnaceKey key = new FurnaceKey(furnace.getLocation());
        final Deque<Contribution> queue = this.queues.get(key);
        if (queue == null || queue.isEmpty()) {
            return;
        }

        // One input item per finished smelt
        final Contribution head = queue.peekFirst();
        final UUID playerId = head.player;
        if (--head.count <= 0) {
            queue.pollFirst();
        }
        if (queue.isEmpty()) {
            this.queues.remove(key);
        }
        this.dirty = true;

        for (final ItemStackSnapshot smelted : event.getSmeltedItems()) {
            if (!smelted.isEmpty()) {
                this.reward(event, playerId, key.world, smelted);
            }
        }
    }

    @Listener(order = Order.POST)
    public void onChangeBlockBreak(final ChangeBlockEvent.Break event) {
        if (this.queues.isEmpty()) {
            return;
        }

        for (final Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (!transaction.isValid()) {
                continue;
            }

            final Location<World> location = transaction.getOriginal().getLocation().orElse(null);
            if (location == null) {
                continue;
            }

            final FurnaceKey key = new FurnaceKey(location);
            if (!this.queues.containsKey(key)) {
                continue;
            }

            // Lighting/unlighting a furnace swaps its block too, only drop the queue if the furnace is really gone
            this.scheduler.createTaskBuilder()
                .delayTicks(1)
                .execute(() -> {
                    if (!(location.getTileEntity().orElse(null) instanceof Furnace)) {
                        this.clearQueue(key);
                    }
                })
                .submit(this.container);
        }
    }

    private void reward(@Nullable final SmeltEvent.Finish event, final UUID playerId, final UUID worldId, final ItemStackSnapshot smelted) {
        final ItemStack stack = smelted.createStack();
        stack.setQuantity(1);

        if (!this.listener.hasItemChain(SmeltEvent.Finish.class, stack)) {
            return;
        }

        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);
        final SkillHolderContainer skillContainer = getParentContainer(service, worldId);
        if (skillContainer == null) {
            return;
        }

        final Player player = Sponge.getServer().getPlayer(playerId).orElse(null);
        final SkillHolder holder = player == null ? null : skillContainer.getHolder(playerId).orElse(null);
        if (player != null && holder != null && isLoaded(holder)) {
            this.listener.rewardItem(event, player, holder, SmeltEvent.Finish.class, stack, 1);
            return;
        }

        this.addPending(playerId, skillContainer.getUniqueId(), stack.createSnapshot(), 1);
    }

    private void payPending(final Player player) {
        final List<PendingSmelt> entries = this.pending.get(player.getUniqueId());
        if (entries == null || entries.isEmpty()) {
            return;
        }

        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);
        final Map<Skill, Double> paid = new HashMap<>();

        final Iterator<PendingSmelt> iterator = entries.iterator();
        while (iterator.hasNext()) {
            final PendingSmelt entry = iterator.next();

            final SkillHolderContainer skillContainer = service.getContainer(entry.container).orElse(null);
            final SkillHolder holder = skillContainer == null ? null : skillContainer.getHolder(player.getUniqueId()).orElse(null);
            if (holder == null || !isLoaded(holder)) {
                // Not in that world group right now, or skills still loading. Keep it for later.
                continue;
            }

            final Map<Skill, Double> xp = this.listener.rewardItem(null, player, holder, SmeltEvent.Finish.class, entry.item.createStack(),
                entry.count);
            xp.forEach((skill, value) -> paid.merge(skill, value, Double::sum));

            iterator.remove();
            this.dirty = true;
        }

        if (entries.isEmpty()) {
            this.pending.remove(player.getUniqueId());
        }

        for (final Map.Entry<Skill, Double> entry : paid.entrySet()) {
            player.sendMessage(Text.of(TextColors.GRAY, "Smelting while you were away earned you ", TextColors.GOLD,
                service.getXPFormat().format(entry.getValue()), TextColors.GRAY, " ", entry.getKey().getName(), " XP."));
        }
    }

    private void maintain() {
        if (!this.pending.isEmpty()) {
            for (final Player player : Sponge.getServer().getOnlinePlayers()) {
                if (this.pending.containsKey(player.getUniqueId())) {
                    this.payPending(player);
                }
            }
        }

        if (this.dirty) {
            this.save();
        }
    }

    private void enqueue(final FurnaceKey key, final UUID playerId, final int count) {
        final Deque<Contribution> queue = this.queues.computeIfAbsent(key, k -> new ArrayDeque<>());
        final Contribution last = queue.peekLast();
        if (last != null && last.player.equals(playerId)) {
            last.count += count;
        } else {
            queue.addLast(new Contribution(playerId, count));
        }
        this.dirty = true;
    }

    private void trimQueue(final FurnaceKey key, final int max) {
        final Deque<Contribution> queue = this.queues.get(key);
        if (queue == null) {
            return;
        }

        int total = 0;
        for (final Contribution contribution : queue) {
            total += contribution.count;
        }

        int excess = total - max;
        while (excess > 0 && !queue.isEmpty()) {
            final Contribution last = queue.peekLast();
            final int removed = Math.min(excess, last.count);
            last.count -= removed;
            excess -= removed;
            if (last.count <= 0) {
                queue.pollLast();
            }
            this.dirty = true;
        }

        if (queue.isEmpty()) {
            this.queues.remove(key);
        }
    }

    private void clearQueue(final FurnaceKey key) {
        if (this.queues.remove(key) != null) {
            this.dirty = true;
        }
    }

    private void addPending(final UUID playerId, final UUID containerId, final ItemStackSnapshot item, final int count) {
        final List<PendingSmelt> entries = this.pending.computeIfAbsent(playerId, k -> new ArrayList<>());
        final ItemStack stack = item.createStack();
        for (final PendingSmelt entry : entries) {
            if (entry.container.equals(containerId) && ItemStackComparators.IGNORE_SIZE.compare(entry.item.createStack(), stack) == 0) {
                entry.count += count;
                this.dirty = true;
                return;
            }
        }
        entries.add(new PendingSmelt(containerId, item, count));
        this.dirty = true;
    }

    /**
     * Logs once if furnace slots can't be told apart, which would mean no smelting XP is ever tracked.
     */
    private void checkSlotTypes(final ClickInventoryEvent event) {
        if (this.checkedSlotTypes || event.getTargetInventory().getArchetype() != InventoryArchetypes.FURNACE) {
            return;
        }
        this.checkedSlotTypes = true;

        for (final Inventory inventory : event.getTargetInventory().slots()) {
            if (inventory instanceof Slot && isInputSlot((Slot) inventory)) {
                return;
            }
        }

        this.logger.warn("Smelting XP: the furnace's input slot could not be identified, items players put into furnaces will not be tracked!");
    }

    private void load() {
        this.queues.clear();
        this.pending.clear();
        this.dirty = false;

        if (Files.notExists(this.dataFile)) {
            return;
        }

        final DataContainer data;
        try (final BufferedReader reader = Files.newBufferedReader(this.dataFile, StandardCharsets.UTF_8)) {
            data = DataFormats.JSON.readFrom(reader);
        } catch (final Exception ex) {
            this.logger.error("Failed to read smelting data from '{}', smelting queues and unpaid smelting XP are lost!", this.dataFile, ex);
            return;
        }

        for (final DataView view : data.getViewList(FURNACES).orElse(new ArrayList<>())) {
            final UUID world = view.getString(WORLD).map(UUID::fromString).orElse(null);
            if (world == null) {
                continue;
            }
            final FurnaceKey key = new FurnaceKey(world, new Vector3i(view.getInt(X).orElse(0), view.getInt(Y).orElse(0),
                view.getInt(Z).orElse(0)));

            for (final DataView entry : view.getViewList(QUEUE).orElse(new ArrayList<>())) {
                final UUID player = entry.getString(PLAYER).map(UUID::fromString).orElse(null);
                final int count = entry.getInt(COUNT).orElse(0);
                if (player != null && count > 0) {
                    this.queues.computeIfAbsent(key, k -> new ArrayDeque<>()).addLast(new Contribution(player, count));
                }
            }
        }

        for (final DataView view : data.getViewList(PENDING).orElse(new ArrayList<>())) {
            final UUID player = view.getString(PLAYER).map(UUID::fromString).orElse(null);
            final UUID skillContainer = view.getString(CONTAINER).map(UUID::fromString).orElse(null);
            final int count = view.getInt(COUNT).orElse(0);
            final ItemStackSnapshot item = view.getView(ITEM)
                .flatMap(itemView -> Sponge.getDataManager().deserialize(ItemStackSnapshot.class, itemView))
                .orElse(null);

            if (player == null || skillContainer == null || item == null || count <= 0) {
                this.logger.warn("Skipping unreadable unpaid smelting XP entry in '{}'.", this.dataFile);
                continue;
            }
            this.pending.computeIfAbsent(player, k -> new ArrayList<>()).add(new PendingSmelt(skillContainer, item, count));
        }
    }

    private void save() {
        final DataContainer data = DataContainer.createNew();
        data.set(VERSION, DATA_VERSION);

        final List<DataView> furnaces = new ArrayList<>();
        this.queues.forEach((key, queue) -> {
            final List<DataView> entries = new ArrayList<>();
            for (final Contribution contribution : queue) {
                entries.add(DataContainer.createNew()
                    .set(PLAYER, contribution.player.toString())
                    .set(COUNT, contribution.count));
            }
            furnaces.add(DataContainer.createNew()
                .set(WORLD, key.world.toString())
                .set(X, key.position.getX())
                .set(Y, key.position.getY())
                .set(Z, key.position.getZ())
                .set(QUEUE, entries));
        });
        data.set(FURNACES, furnaces);

        final List<DataView> pendingEntries = new ArrayList<>();
        this.pending.forEach((player, entries) -> {
            for (final PendingSmelt entry : entries) {
                pendingEntries.add(DataContainer.createNew()
                    .set(PLAYER, player.toString())
                    .set(CONTAINER, entry.container.toString())
                    .set(ITEM, entry.item.toContainer())
                    .set(COUNT, entry.count));
            }
        });
        data.set(PENDING, pendingEntries);

        final Path temp = this.dataFile.resolveSibling(DATA_FILE + ".tmp");
        try {
            Files.createDirectories(this.dataFile.getParent());
            try (final BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                DataFormats.JSON.writeTo(writer, data);
            }
            try {
                Files.move(temp, this.dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (final AtomicMoveNotSupportedException ex) {
                Files.move(temp, this.dataFile, StandardCopyOption.REPLACE_EXISTING);
            }
            this.dirty = false;
        } catch (final IOException ex) {
            this.logger.error("Failed to save smelting data to '{}'!", this.dataFile, ex);
        }
    }

    private static boolean isInputSlot(final Slot slot) {
        if (slot instanceof InputSlot) {
            return !(slot instanceof FuelSlot) && !(slot instanceof OutputSlot);
        }
        try {
            final Slot root = slot.transform();
            return root instanceof InputSlot && !(root instanceof FuelSlot) && !(root instanceof OutputSlot);
        } catch (final RuntimeException ex) {
            return false;
        }
    }

    @Nullable
    private static FurnaceKey furnaceOf(final Slot slot) {
        Inventory current;
        try {
            current = slot.transform();
        } catch (final RuntimeException ex) {
            current = slot;
        }

        for (int depth = 0; depth < 8 && current != null; depth++) {
            if (current instanceof Furnace) {
                return new FurnaceKey(((Furnace) current).getLocation());
            }
            if (current instanceof CarriedInventory) {
                final Object carrier = ((CarriedInventory<?>) current).getCarrier().orElse(null);
                if (carrier instanceof Furnace) {
                    return new FurnaceKey(((Furnace) carrier).getLocation());
                }
            }
            final Inventory parent = current.parent();
            if (parent == current) {
                break;
            }
            current = parent;
        }
        return null;
    }

    private static boolean isSameItem(final ItemStackSnapshot a, final ItemStackSnapshot b) {
        return a.getType() == b.getType() && ItemStackComparators.IGNORE_SIZE.compare(a.createStack(), b.createStack()) == 0;
    }

    @Nullable
    private static SkillHolderContainer getParentContainer(final SkillService service, final UUID worldId) {
        final SkillHolderContainer worldContainer = service.getContainer(worldId).orElse(null);
        return worldContainer == null ? null : service.getParentContainer(worldContainer).orElse(null);
    }

    private static boolean isLoaded(final SkillHolder holder) {
        for (final Skill skill : holder.getSkills().values()) {
            if (skill instanceof SkillImpl && !((SkillImpl) skill).isLoaded()) {
                return false;
            }
        }
        return true;
    }

    private static final class Contribution {

        final UUID player;
        int count;

        Contribution(final UUID player, final int count) {
            this.player = player;
            this.count = count;
        }
    }

    private static final class PendingSmelt {

        final UUID container;
        final ItemStackSnapshot item;
        int count;

        PendingSmelt(final UUID container, final ItemStackSnapshot item, final int count) {
            this.container = container;
            this.item = item;
            this.count = count;
        }
    }

    private static final class FurnaceKey {

        final UUID world;
        final Vector3i position;

        FurnaceKey(final Location<World> location) {
            this(location.getExtent().getUniqueId(), location.getBlockPosition());
        }

        FurnaceKey(final UUID world, final Vector3i position) {
            this.world = world;
            this.position = position;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof FurnaceKey)) {
                return false;
            }
            final FurnaceKey other = (FurnaceKey) o;
            return this.position.equals(other.position) && this.world.equals(other.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.world, this.position);
        }
    }
}
