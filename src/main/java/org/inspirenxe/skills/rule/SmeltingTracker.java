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
package org.inspirenxe.skills.rule;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.inspirenxe.skills.Tasks;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillHolderContainer;
import org.inspirenxe.skills.api.Skills;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.furnace.FurnaceBlockEntity;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.entity.CookingEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.container.ClickContainerEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.slot.FuelSlot;
import org.spongepowered.api.item.inventory.slot.InputSlot;
import org.spongepowered.api.item.inventory.slot.OutputSlot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.math.vector.Vector3i;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Smelting XP: the player who puts items into a furnace's input slot is credited when those items finish smelting.
 *
 * <ul>
 *     <li>Every furnace keeps a queue of who loaded how many of the items in its input slot, oldest first. Each finished smelt
 *     ({@link CookingEvent.Finish}) credits the head of the queue with the rewards of the {@code smelt} rules for the result. Anything
 *     that fires {@code CookingEvent} (furnaces, blast furnaces, smokers and machines built on them) works.</li>
 *     <li>Only items players put in by hand count. Items fed by hoppers or pipes are not in the queue and earn nothing; who takes the
 *     result out doesn't matter.</li>
 *     <li>A queue never holds more than the input slot does: taking items back out trims it (newest first), swapping the stack for
 *     another item clears it, breaking the furnace drops it.</li>
 *     <li>A player who is offline (or has no loaded skills in the furnace's world) when an item finishes is paid when they are online
 *     again, at the levels they have then.</li>
 *     <li>Queues and unpaid rewards are saved to {@code smelting-data.conf} in the config directory, so they survive restarts.</li>
 * </ul>
 */
public final class SmeltingTracker {

    private static final String DATA_FILE = "smelting-data.conf";
    private static final int DATA_VERSION = 1;
    private static final Duration MAINTENANCE_INTERVAL = Duration.ofSeconds(30);
    private static final long JOIN_PAYOUT_DELAY_TICKS = 100;

    private final Logger logger;
    private final RuleListener listener;
    private final Path dataFile;
    // Furnace -> who loaded the items in its input slot, oldest first
    private final Map<FurnaceKey, Deque<Contribution>> queues = new HashMap<>();
    // Player -> furnace they last opened, used when a clicked input slot doesn't lead back to its furnace
    private final Map<UUID, FurnaceKey> openFurnaces = new HashMap<>();
    // Player -> rewards not paid yet (offline, or no loaded skills in the furnace's world)
    private final Map<UUID, List<PendingSmelt>> pending = new HashMap<>();
    private boolean dirty;
    private boolean checkedSlotTypes;

    public SmeltingTracker(final Logger logger, final RuleListener listener, final Path configDir) {
        this.logger = logger;
        this.listener = listener;
        this.dataFile = configDir.resolve(SmeltingTracker.DATA_FILE);
    }

    public void start() {
        this.load();
        Tasks.repeat("Skills - Smelting maintenance", SmeltingTracker.MAINTENANCE_INTERVAL, this::maintain);
    }

    public void stop() {
        this.save();
    }

    @Listener
    public void onJoin(final ServerSideConnectionEvent.Join event) {
        final UUID playerId = event.player().uniqueId();
        if (!this.pending.containsKey(playerId)) {
            return;
        }
        // Give the player's skills time to load from the database, maintenance retries if they aren't loaded yet
        Tasks.later(SmeltingTracker.JOIN_PAYOUT_DELAY_TICKS, () -> Sponge.server().player(playerId).ifPresent(this::payPending));
    }

    @Listener
    public void onLeave(final ServerSideConnectionEvent.Leave event) {
        this.openFurnaces.remove(event.player().uniqueId());
    }

    @Listener(order = Order.POST)
    public void onInteractBlock(final InteractBlockEvent.Secondary.Post event, @Root final ServerPlayer player) {
        event.block().location()
                .flatMap(ServerLocation::blockEntity)
                .filter(FurnaceBlockEntity.class::isInstance)
                .ifPresent(furnace -> this.openFurnaces.put(player.uniqueId(), FurnaceKey.of(furnace.serverLocation())));
    }

    @Listener
    public void onCloseContainer(final InteractContainerEvent.Close event, @Root final ServerPlayer player) {
        this.openFurnaces.remove(player.uniqueId());
    }

    @Listener(order = Order.POST)
    public void onClickContainer(final ClickContainerEvent event, @Root final ServerPlayer player) {
        FurnaceKey furnace = null;
        for (final Inventory viewed : event.container().viewed()) {
            if (viewed instanceof CarriedInventory<?> carried && carried.carrier().orElse(null) instanceof FurnaceBlockEntity entity) {
                furnace = FurnaceKey.of(entity.serverLocation());
                break;
            }
        }
        if (furnace == null) {
            furnace = this.openFurnaces.get(player.uniqueId());
        }
        if (furnace == null) {
            return;
        }
        final boolean canEarn = !player.gameMode().get().equals(GameModes.CREATIVE.get());
        boolean sawInput = false;
        for (final SlotTransaction transaction : event.transactions()) {
            if (!transaction.isValid() || !SmeltingTracker.isInputSlot(transaction.slot())) {
                continue;
            }
            sawInput = true;
            final ItemStackSnapshot original = transaction.original();
            final ItemStackSnapshot result = transaction.finalReplacement();
            final boolean sameItem = original.isEmpty() || result.isEmpty() || original.type().equals(result.type());
            if (!sameItem) {
                // Stack swapped for another item, everything queued for the old one is gone
                this.clearQueue(furnace);
            }
            final int added = result.isEmpty() ? 0 : sameItem ? result.quantity() - original.quantity() : result.quantity();
            if (added > 0 && canEarn) {
                this.enqueue(furnace, player.uniqueId(), added);
            }
            // Taken out (or never tracked): the queue can't hold more than the slot does
            this.trimQueue(furnace, result.isEmpty() ? 0 : result.quantity());
        }
        if (!sawInput && !this.checkedSlotTypes && !event.transactions().isEmpty()) {
            this.checkSlotTypes(event);
        }
    }

    @Listener
    public void onCookFinish(final CookingEvent.Finish event) {
        if (this.queues.isEmpty()) {
            return;
        }
        final BlockEntity furnace = event.blockEntity();
        final FurnaceKey key = FurnaceKey.of(furnace.serverLocation());
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
        final ItemType result = SmeltingTracker.resultOf(event);
        if (result != null) {
            this.reward(event, playerId, key.world, result);
        }
    }

    @Listener(order = Order.POST)
    public void onChangeBlock(final ChangeBlockEvent.All event) {
        if (this.queues.isEmpty()) {
            return;
        }
        for (final BlockTransaction transaction : event.transactions()) {
            if (!transaction.isValid()) {
                continue;
            }
            final ServerLocation location = transaction.original().location().orElse(null);
            if (location == null) {
                continue;
            }
            final FurnaceKey key = FurnaceKey.of(location);
            if (!this.queues.containsKey(key)) {
                continue;
            }
            // Lighting/unlighting a furnace changes its block state too, only drop the queue if the furnace is really gone
            Tasks.later(1, () -> {
                if (!(location.blockEntity().orElse(null) instanceof FurnaceBlockEntity)) {
                    this.clearQueue(key);
                }
            });
        }
    }

    private static ItemType resultOf(final CookingEvent.Finish event) {
        for (final SlotTransaction transaction : event.transactions()) {
            final ItemStackSnapshot result = transaction.finalReplacement();
            if (!result.isEmpty() && result.quantity() > (transaction.original().isEmpty() ? 0 : transaction.original().quantity())
                    && (transaction.original().isEmpty() || transaction.original().type().equals(result.type()))) {
                return result.type();
            }
        }
        return event.recipe().map(recipe -> recipe.exemplaryResult().type()).orElse(null);
    }

    private void reward(final CookingEvent.Finish event, final UUID playerId, final ResourceKey world, final ItemType result) {
        if (!this.listener.rules().matchesAny(Trigger.SMELT, result) || !Skills.isAvailable()) {
            return;
        }
        final SkillHolderContainer container = Skills.service().containerFor(world).orElse(null);
        if (container == null) {
            return;
        }
        final ServerPlayer player = Sponge.server().player(playerId).orElse(null);
        final SkillHolder holder = player == null ? null : container.holder(playerId).orElse(null);
        if (player != null && holder != null && holder.isLoaded()) {
            this.listener.rewardItem(event, player, holder, Trigger.SMELT, result, 1);
            return;
        }
        this.addPending(playerId, container.key(), result, 1);
    }

    private void payPending(final ServerPlayer player) {
        final List<PendingSmelt> entries = this.pending.get(player.uniqueId());
        if (entries == null || entries.isEmpty() || !Skills.isAvailable()) {
            return;
        }
        final Map<Skill, Double> paid = new HashMap<>();
        final Iterator<PendingSmelt> iterator = entries.iterator();
        while (iterator.hasNext()) {
            final PendingSmelt entry = iterator.next();
            final SkillHolder holder = Skills.service().container(entry.container)
                    .flatMap(container -> container.holder(player.uniqueId()))
                    .orElse(null);
            if (holder == null || !holder.isLoaded()) {
                // Not in that world group right now, or skills still loading. Keep it for later.
                continue;
            }
            final ItemType type = RegistryTypes.ITEM_TYPE.get().<ItemType>findValue(entry.item).orElse(null);
            if (type != null) {
                this.listener.rewardItem(null, player, holder, Trigger.SMELT, type, entry.count)
                        .forEach((skill, xp) -> paid.merge(skill, xp, Double::sum));
            }
            iterator.remove();
            this.dirty = true;
        }
        if (entries.isEmpty()) {
            this.pending.remove(player.uniqueId());
        }
        for (final Map.Entry<Skill, Double> entry : paid.entrySet()) {
            player.sendMessage(Component.text()
                    .append(Component.text("Smelting while you were away earned you ", NamedTextColor.GRAY))
                    .append(Component.text(Skills.service().xpFormat().format(entry.getValue()), NamedTextColor.GOLD))
                    .append(Component.text(" " + entry.getKey().type().name() + " XP.", NamedTextColor.GRAY))
                    .build());
        }
    }

    private void maintain() {
        if (!this.pending.isEmpty()) {
            for (final ServerPlayer player : Sponge.server().onlinePlayers()) {
                if (this.pending.containsKey(player.uniqueId())) {
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

    private void addPending(final UUID playerId, final ResourceKey container, final ItemType type, final int count) {
        final ResourceKey item = type.key(RegistryTypes.ITEM_TYPE);
        final List<PendingSmelt> entries = this.pending.computeIfAbsent(playerId, k -> new ArrayList<>());
        for (final PendingSmelt entry : entries) {
            if (entry.container.equals(container) && entry.item.equals(item)) {
                entry.count += count;
                this.dirty = true;
                return;
            }
        }
        entries.add(new PendingSmelt(container, item, count));
        this.dirty = true;
    }

    /**
     * Logs once if the furnace input slot can't be told apart, which would mean no smelting XP is ever tracked.
     */
    private void checkSlotTypes(final ClickContainerEvent event) {
        this.checkedSlotTypes = true;
        for (final Slot slot : event.container().slots()) {
            if (SmeltingTracker.isInputSlot(slot)) {
                return;
            }
        }
        this.logger.warn("Smelting XP: the furnace's input slot could not be identified, items players put into furnaces will not be "
                + "tracked!");
    }

    private static boolean isInputSlot(final Slot slot) {
        if (SmeltingTracker.isPlainInput(slot)) {
            return true;
        }
        try {
            return SmeltingTracker.isPlainInput(slot.viewedSlot());
        } catch (final RuntimeException e) {
            return false;
        }
    }

    private static boolean isPlainInput(final Slot slot) {
        return slot instanceof InputSlot && !(slot instanceof FuelSlot) && !(slot instanceof OutputSlot);
    }

    // Persistence

    private void load() {
        this.queues.clear();
        this.pending.clear();
        this.dirty = false;
        if (Files.notExists(this.dataFile)) {
            return;
        }
        final ConfigurationNode root;
        try {
            root = HoconConfigurationLoader.builder().path(this.dataFile).build().load();
        } catch (final Exception e) {
            this.logger.error("Failed to read the smelting data from '{}', smelting queues and unpaid smelting XP are lost!", this.dataFile, e);
            return;
        }
        for (final ConfigurationNode node : root.node("furnaces").childrenList()) {
            try {
                final FurnaceKey key = new FurnaceKey(ResourceKey.resolve(node.node("world").getString("")),
                        new Vector3i(node.node("x").getInt(), node.node("y").getInt(), node.node("z").getInt()));
                for (final ConfigurationNode entry : node.node("queue").childrenList()) {
                    final UUID player = UUID.fromString(entry.node("player").getString(""));
                    final int count = entry.node("count").getInt();
                    if (count > 0) {
                        this.queues.computeIfAbsent(key, k -> new ArrayDeque<>()).addLast(new Contribution(player, count));
                    }
                }
            } catch (final RuntimeException e) {
                this.logger.warn("Skipping an unreadable furnace in '{}'.", this.dataFile);
            }
        }
        for (final ConfigurationNode node : root.node("pending").childrenList()) {
            try {
                final UUID player = UUID.fromString(node.node("player").getString(""));
                final ResourceKey container = ResourceKey.resolve(node.node("container").getString(""));
                final ResourceKey item = ResourceKey.resolve(node.node("item").getString(""));
                final int count = node.node("count").getInt();
                if (count > 0) {
                    this.pending.computeIfAbsent(player, k -> new ArrayList<>()).add(new PendingSmelt(container, item, count));
                }
            } catch (final RuntimeException e) {
                this.logger.warn("Skipping an unreadable unpaid smelting XP entry in '{}'.", this.dataFile);
            }
        }
    }

    private void save() {
        final Path temp = this.dataFile.resolveSibling(SmeltingTracker.DATA_FILE + ".tmp");
        try {
            final HoconConfigurationLoader loader = HoconConfigurationLoader.builder().path(temp).build();
            final CommentedConfigurationNode root = loader.createNode();
            root.node("version").set(SmeltingTracker.DATA_VERSION);
            final CommentedConfigurationNode furnaces = root.node("furnaces");
            for (final Map.Entry<FurnaceKey, Deque<Contribution>> entry : this.queues.entrySet()) {
                final CommentedConfigurationNode node = furnaces.appendListNode();
                node.node("world").set(entry.getKey().world.asString());
                node.node("x").set(entry.getKey().position.x());
                node.node("y").set(entry.getKey().position.y());
                node.node("z").set(entry.getKey().position.z());
                final CommentedConfigurationNode queue = node.node("queue");
                for (final Contribution contribution : entry.getValue()) {
                    final CommentedConfigurationNode item = queue.appendListNode();
                    item.node("player").set(contribution.player.toString());
                    item.node("count").set(contribution.count);
                }
            }
            final CommentedConfigurationNode pendingNode = root.node("pending");
            for (final Map.Entry<UUID, List<PendingSmelt>> entry : this.pending.entrySet()) {
                for (final PendingSmelt smelt : entry.getValue()) {
                    final CommentedConfigurationNode node = pendingNode.appendListNode();
                    node.node("player").set(entry.getKey().toString());
                    node.node("container").set(smelt.container.asString());
                    node.node("item").set(smelt.item.asString());
                    node.node("count").set(smelt.count);
                }
            }
            Files.createDirectories(this.dataFile.getParent());
            loader.save(root);
            try {
                Files.move(temp, this.dataFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (final AtomicMoveNotSupportedException e) {
                Files.move(temp, this.dataFile, StandardCopyOption.REPLACE_EXISTING);
            }
            this.dirty = false;
        } catch (final Exception e) {
            this.logger.error("Failed to save the smelting data to '{}'!", this.dataFile, e);
        }
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

        final ResourceKey container;
        final ResourceKey item;
        int count;

        PendingSmelt(final ResourceKey container, final ResourceKey item, final int count) {
            this.container = container;
            this.item = item;
            this.count = count;
        }
    }

    private record FurnaceKey(ResourceKey world, Vector3i position) {

        static FurnaceKey of(final ServerLocation location) {
            return new FurnaceKey(location.worldKey(), location.blockPosition());
        }
    }
}
