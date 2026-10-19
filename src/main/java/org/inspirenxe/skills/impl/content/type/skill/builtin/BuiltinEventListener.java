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

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.toolbox.inject.event.Witness;
import com.flowpowered.math.vector.Vector3i;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.inspirenxe.skills.api.SkillHolderContainer;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.event.ChangeExperienceEvent;
import org.inspirenxe.skills.api.result.Result;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.result.experience.ExperienceResult;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.BlockChain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.Chain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.ItemChain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.result.BuiltinResult;
import org.inspirenxe.skills.impl.content.type.skill.builtin.registar.CraftingRegistar;
import org.inspirenxe.skills.impl.content.type.skill.builtin.registar.DiggerRegistar;
import org.inspirenxe.skills.impl.content.type.skill.builtin.registar.FarmingRegistar;
import org.inspirenxe.skills.impl.content.type.skill.builtin.registar.MiningRegistar;
import org.inspirenxe.skills.impl.content.type.skill.builtin.registar.WoodcuttingRegistar;
import org.inspirenxe.skills.impl.util.function.TriFunction;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.AffectEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.LocatableBlock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

// TODO Plan B. Move to scripting engine later.

/**
 * Applies the builtin skill chains to game events.
 *
 * <p><b>Two phase processing.</b> Chains are evaluated at {@link Order#EARLY}: level gates deny the action there (cancelling the event,
 * invalidating the block transaction, removing the drop, or denying the item use). XP and money are NOT paid at that point; the rewards
 * are remembered per event and only paid at {@link Order#POST}, once every other plugin (claims, jails, protection) had its say, and
 * only if the event was not cancelled and, per reward, the block transaction is still valid / the dropped item is still part of the
 * event.</p>
 *
 * <p><b>Placement anti-farming.</b> Rewards for placing blocks (planting crops, hoeing farmland) are guarded so that place, break,
 * re-place loops can't be repeated for XP and money:</p>
 * <ul>
 *     <li>Only placements caused directly by a player are rewarded (no falling blocks, pistons, block ticks, ...).</li>
 *     <li>A placement that converts an existing, player placed block (e.g. hoeing dirt a player placed) is not rewarded.</li>
 *     <li>Every rewarded placement is remembered per player and position. Placing at a remembered position again is not rewarded
 *     until {@link #PLACEMENT_REWARD_COOLDOWN} has passed. Breaking the block again does not clear the mark (breaking an immature crop
 *     forfeits the replant reward), only a <i>rewarded</i> harvest of that position (a break or drop that paid XP/money, i.e. a mature
 *     crop) clears it, so normal plant, grow, harvest, replant farming keeps paying.</li>
 *     <li>Memory is bounded: at most {@link #PLACEMENT_HISTORY_MAX_PER_PLAYER} positions per player (oldest evicted first), expired
 *     marks are pruned whenever players join or quit. A quitting player's marks are kept until they expire so re-logging doesn't reset
 *     the cooldown.</li>
 * </ul>
 */
@Singleton
public final class BuiltinEventListener implements Witness {

    /**
     * How long a rewarded placement position stays non-rewardable for the same player (unless harvested with a reward).
     */
    private static final long PLACEMENT_REWARD_COOLDOWN = TimeUnit.MINUTES.toMillis(10);
    private static final int PLACEMENT_HISTORY_MAX_PER_PLAYER = 4096;
    private static final long DENY_MESSAGE_THROTTLE = 5000;
    private static final long INTERACT_DENY_MESSAGE_THROTTLE = 3500;
    private static final long PENDING_REWARD_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
    private static final int PENDING_REWARD_SWEEP_THRESHOLD = 64;

    private final Logger logger;
    private final ServiceManager serviceManager;
    private final Map<Class<? extends Event>, Map<SkillType, List<EventFeedback>>> messageBuilders = new HashMap<>();
    private final Map<Class<? extends Event>, Map<SkillType, List<EventFeedback>>> effectBuilders = new HashMap<>();
    private final Map<Class<? extends Event>, Map<SkillType, List<BlockChain>>> blockChains = new HashMap<>();
    private final Map<Class<? extends Event>, Map<SkillType, List<ItemChain>>> itemChains = new HashMap<>();

    // Resolved (event class -> skill type -> chains indexed by block/item type). Rebuilt lazily whenever chains change.
    private final Map<Class<?>, Map<SkillType, ChainIndex<BlockChain, BlockType>>> blockChainIndex = new HashMap<>();
    private final Map<Class<?>, Map<SkillType, ChainIndex<ItemChain, ItemType>>> itemChainIndex = new HashMap<>();

    // Player -> chain -> last time a deny message for that chain was sent to that player
    private final Map<UUID, Map<Chain<?>, Long>> denyTimers = new HashMap<>();

    // Event (identity) -> rewards computed at EARLY, paid at POST
    private final Map<Event, PendingRewards> pendingRewards = new IdentityHashMap<>();

    // Player -> rewarded placement positions
    private final Map<UUID, PlacementHistory> placementHistory = new HashMap<>();

    @Inject
    public BuiltinEventListener(final Logger logger, final ServiceManager serviceManager) {
        this.logger = logger;
        this.serviceManager = serviceManager;
    }

    @Listener(order = Order.LAST)
    public void onGameStartedServer(final GameStartedServerEvent event) {
        this.configure();
    }

    @Listener
    public void onGameReload(final GameReloadEvent event) {
        this.configure();
    }

    @Listener
    public void onClientConnectionJoin(final ClientConnectionEvent.Join event) {
        this.prunePlacementHistory(System.currentTimeMillis());
    }

    @Listener
    public void onClientConnectionDisconnect(final ClientConnectionEvent.Disconnect event, @Root final Player player) {
        this.denyTimers.remove(player.getUniqueId());
        // The player's own placement history is intentionally kept until it expires, so re-logging doesn't reset the cooldown.
        this.prunePlacementHistory(System.currentTimeMillis());
    }

    @Listener(order = Order.EARLY)
    public void onChangeBlockBreak(final ChangeBlockEvent.Break event, @First final Player player) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        // Patch getting XP for blocks that fall
        if (event.getCause().containsType(FallingBlock.class)) {
            return;
        }

        this.handleChangeBlock(service, event, player, true, RewardKind.BREAK);
    }

    @Listener(order = Order.EARLY)
    public void onChangeBlockModify(final ChangeBlockEvent.Modify event, @Root final Player player) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        this.handleChangeBlock(service, event, player, false, RewardKind.OTHER);
    }

    @Listener(order = Order.EARLY)
    public void onChangeBlockPlace(final ChangeBlockEvent.Place event, @Root final Player player) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        // Only reward placements done by the player themselves, not by falling blocks, pistons, block ticks, etc.
        final Cause cause = event.getCause();
        if (cause.containsType(FallingBlock.class) || cause.containsType(LocatableBlock.class)) {
            return;
        }

        this.handleChangeBlock(service, event, player, false, RewardKind.PLACE);
    }

    @Listener(order = Order.EARLY)
    public void onInteractItem(final InteractItemEvent event, @Root final Player player) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        if (this.handleInteractItem(service, event, event.getClass(), event.getItemStack().createStack(), player)) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.EARLY)
    public void onInteractBlock(final InteractBlockEvent event, @Root final Player player) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        if (event instanceof InteractBlockEvent.Secondary) {
            // Right click: only deny the USE of a too high level tool (e.g. hoeing), never the block interaction itself, so opening
            // chests, doors, etc. while holding such a tool still works. Vanilla only uses the item if the block wasn't activated.
            final InteractBlockEvent.Secondary secondary = (InteractBlockEvent.Secondary) event;
            final HandType hand = secondary.getHandType();
            final ItemStack itemStack = player.getItemInHand(hand).orElse(null);
            if (itemStack != null) {
                final Class<? extends Event> filter = hand == HandTypes.OFF_HAND ? InteractItemEvent.Secondary.OffHand.class :
                    InteractItemEvent.Secondary.MainHand.class;
                if (this.handleInteractItem(service, event, filter, itemStack, player)) {
                    secondary.setUseItemResult(Tristate.FALSE);
                }
            }
        } else {
            // Left click (hitting/breaking the block): the tool is used, deny the whole interaction.
            // If the Player has an item in their hand, give that priority and allow it to cancel the block interaction (main hand -> offhand -> block).
            ItemStack itemStack = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
            if (itemStack != null && this.handleInteractItem(service, event, InteractItemEvent.Primary.MainHand.class, itemStack, player)) {
                event.setCancelled(true);
                return;
            }

            itemStack = player.getItemInHand(HandTypes.OFF_HAND).orElse(null);
            if (itemStack != null && this.handleInteractItem(service, event, InteractItemEvent.Primary.OffHand.class, itemStack, player)) {
                event.setCancelled(true);
                return;
            }
        }

        this.handleInteractBlock(service, event, player);
    }

    @Listener(order = Order.EARLY)
    public void onCraftItemCraft(final CraftItemEvent.Craft event, @Root final Player player) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        this.handleCraftItem(service, event, player);
    }

    @Listener(order = Order.EARLY)
    public void onDropItemDestruct(final DropItemEvent.Destruct event, @Root final BlockSnapshot snapshot, @First final Player player) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);
        if (player.getWorld().getTileEntity(snapshot.getPosition()).isPresent()) {
            return;
        }
        this.handleDropItemEvent(service, event, snapshot, player);
    }

    // Rewards are paid once the final outcome of the event is known. @IsCancelled(UNDEFINED) so pending rewards of cancelled events are
    // discarded as well.

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.UNDEFINED)
    public void onChangeBlockPost(final ChangeBlockEvent event) {
        this.payPendingRewards(event);
    }

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.UNDEFINED)
    public void onInteractItemPost(final InteractItemEvent event) {
        this.payPendingRewards(event);
    }

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.UNDEFINED)
    public void onInteractBlockPost(final InteractBlockEvent event) {
        this.payPendingRewards(event);
    }

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.UNDEFINED)
    public void onCraftItemCraftPost(final CraftItemEvent.Craft event) {
        this.payPendingRewards(event);
    }

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.UNDEFINED)
    public void onDropItemDestructPost(final DropItemEvent.Destruct event) {
        this.payPendingRewards(event);
    }

    @Listener(order = Order.EARLY)
    public void onChangeExperiencePost(final ChangeExperienceEvent.Post event, @First final Player player) {

        List<EventFeedback> messages = null;
        List<EventFeedback> effects = null;

        final Cause cause = event.getCause();

        Class<? extends Event> parentEventClazz = null;

        final Event causeEvent = cause.first(Event.class).orElse(null);
        if (causeEvent != null) {
            parentEventClazz = causeEvent.getClass();
        } else if (cause.containsType(CommandCallable.class)) {
            parentEventClazz = Event.class;
        }

        final Class<? extends Event> eventClazz = parentEventClazz;

        if (eventClazz != null) {
            messages = this.messageBuilders.entrySet()
                .stream()
                .filter(kv -> kv.getKey().isAssignableFrom(eventClazz))
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new HashMap<>()).entrySet()
                .stream()
                .filter(kv -> kv.getKey() == event.getSkillType())
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new ArrayList<>());

            effects = this.effectBuilders.entrySet()
                .stream()
                .filter(kv -> kv.getKey().isAssignableFrom(eventClazz))
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new HashMap<>()).entrySet()
                .stream()
                .filter(kv -> kv.getKey() == event.getSkillType())
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new ArrayList<>());
        }

        final double xpDiff = event.getExperienceDifference();

        if (messages != null) {
            messages
                .stream()
                .filter(v -> v.xpGained != null)
                .forEach(v -> v.xpGained.accept(cause, event.getSkill(), xpDiff));
        }

        if (event instanceof ChangeExperienceEvent.Post.Level) {

            ChangeExperienceEvent.Post.Level levelEvent = (ChangeExperienceEvent.Post.Level) event;

            if (xpDiff < 0) {
                return;
            }

            if (messages != null) {
                messages
                    .stream()
                    .filter(v -> v.levelGained != null)
                    .forEach(v -> v.levelGained.accept(cause, event.getSkill(), levelEvent.getLevel()));
            }

            if (effects != null) {
                try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    frame.pushCause(event.getSkill());

                    effects
                        .stream()
                        .filter(v -> v.levelGained != null)
                        .forEach(effect -> effect.levelGained.accept(cause, event.getSkill(), levelEvent.getLevel()));
                }
            }
        } else {
            if (effects != null) {
                // Xp effects
                effects
                    .stream()
                    .filter(v -> v.xpGained != null)
                    .forEach(effect -> effect.xpGained.accept(cause, event.getSkill(), xpDiff));
            }
        }
    }

    @Listener(order = Order.EARLY)
    public void onDamageEntity(final DamageEntityEvent event, @Root final EntityDamageSource source, @First final Skill skill) {
        if (source.getSource() instanceof Firework) {
            event.setCancelled(true);
        }
    }

    private void configure() {
        this.blockChains.clear();
        this.effectBuilders.clear();
        this.itemChains.clear();
        this.messageBuilders.clear();
        this.blockChainIndex.clear();
        this.itemChainIndex.clear();
        this.denyTimers.clear();
        this.pendingRewards.clear();

        MiningRegistar.configure();
        CraftingRegistar.configure();
        FarmingRegistar.configure();
        WoodcuttingRegistar.configure();
        DiggerRegistar.configure();
    }

    @Nullable
    private SkillHolder getSkillHolder(final SkillService service, final Player player) {
        if (player.gameMode().get() == GameModes.CREATIVE) {
            return null;
        }

        final SkillHolderContainer worldContainer = service.getContainer(player.getWorld().getUniqueId()).orElse(null);
        if (worldContainer == null) {
            return null;
        }

        final SkillHolderContainer container = service.getParentContainer(worldContainer).orElse(null);
        if (container == null) {
            return null;
        }

        return container.getHolder(player.getUniqueId()).orElse(null);
    }

    private void handleChangeBlock(final SkillService service, final ChangeBlockEvent event, final Player player, final boolean originalState,
        final RewardKind kind) {

        final SkillHolder skillHolder = this.getSkillHolder(service, player);
        if (skillHolder == null) {
            return;
        }

        final Map<SkillType, ChainIndex<BlockChain, BlockType>> eventChains = this.getBlockChainsFor(event.getClass());
        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = skillHolder.getSkills().values();

        for (final Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (!transaction.isValid()) {
                continue;
            }

            final BlockSnapshot snapshot = originalState ? transaction.getOriginal() : transaction.getFinal();

            final Collection<BuiltinResult> transactionResults = this.processBlockSnapshotFor(event.getCause(), skills, eventChains, snapshot);

            boolean denied = false;
            for (final BuiltinResult result : transactionResults) {
                if (result.getType() == Result.Type.CANCELLED) {
                    denied = true;
                    this.sendDenyMessage(player, event.getCause(), result, DENY_MESSAGE_THROTTLE);
                }
            }

            if (denied) {
                // Nothing on this block is rewarded, not even skills whose gate passed
                transaction.setValid(false);
                continue;
            }

            this.queueRewards(event, player, kind, transactionResults, transaction, null, new PositionKey(snapshot));
        }
    }

    /**
     * @return True if a level gate denied the use of the item (the caller decides how to deny), false otherwise. Rewards are queued
     * only when not denied.
     */
    private boolean handleInteractItem(final SkillService service, final Event event, final Class<? extends Event> classFilter,
        final ItemStack eventStack, final Player player) {

        final SkillHolder skillHolder = this.getSkillHolder(service, player);
        if (skillHolder == null) {
            return false;
        }

        final Map<SkillType, ChainIndex<ItemChain, ItemType>> eventChains = this.getItemChainsFor(classFilter);
        if (eventChains.isEmpty()) {
            return false;
        }

        final Collection<Skill> skills = skillHolder.getSkills().values();

        final Collection<BuiltinResult> results = this.handleItemStack(skills, eventChains, eventStack);

        boolean denied = false;
        for (final BuiltinResult result : results) {
            if (result.getType() == Result.Type.CANCELLED) {
                denied = true;
                this.sendDenyMessage(player, event.getCause(), result, INTERACT_DENY_MESSAGE_THROTTLE);
            }
        }

        if (denied) {
            return true;
        }

        this.queueRewards(event, player, RewardKind.OTHER, results, null, null, null);
        return false;
    }

    private void handleInteractBlock(final SkillService service, final InteractBlockEvent event, final Player player) {
        final SkillHolder holder = this.getSkillHolder(service, player);
        if (holder == null) {
            return;
        }

        final Map<SkillType, ChainIndex<BlockChain, BlockType>> eventChains = this.getBlockChainsFor(event.getClass());
        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = holder.getSkills().values();

        final Collection<BuiltinResult> results = this.processBlockSnapshotFor(event.getCause(), skills, eventChains, event.getTargetBlock());

        boolean denied = false;
        for (final BuiltinResult result : results) {
            if (result.getType() == Result.Type.CANCELLED) {
                denied = true;
                this.sendDenyMessage(player, event.getCause(), result, INTERACT_DENY_MESSAGE_THROTTLE);
            }
        }

        if (denied) {
            event.setCancelled(true);
            return;
        }

        this.queueRewards(event, player, RewardKind.OTHER, results, null, null, null);
    }

    private void handleCraftItem(final SkillService service, final CraftItemEvent.Craft event, final Player player) {
        final SkillHolder holder = this.getSkillHolder(service, player);
        if (holder == null) {
            return;
        }

        final Map<SkillType, ChainIndex<ItemChain, ItemType>> eventChains = this.getItemChainsFor(event.getClass());
        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = holder.getSkills().values();

        final List<BuiltinResult> results = new ArrayList<>();

        final List<ItemStack> stacks = new ArrayList<>();

        final ItemStack crafted = event.getCrafted().createStack();

        final CraftingRecipe recipe = event.getRecipe().orElse(null);
        if (recipe != null) {
            final ItemStack defaultResult = recipe.getExemplaryResult().createStack();
            stacks.add(defaultResult);
        } else {
            stacks.add(crafted);
        }

        for (final ItemStack stack : stacks) {
            results.addAll(this.handleItemStack(skills, eventChains, stack));
        }

        boolean denied = false;
        for (final BuiltinResult result : results) {
            if (result.getType() == Result.Type.CANCELLED) {
                denied = true;
                this.sendDenyMessage(player, event.getCause(), result, DENY_MESSAGE_THROTTLE);
            }
        }

        if (denied) {
            event.setCancelled(true);
            return;
        }

        this.queueRewards(event, player, RewardKind.OTHER, results, null, null, null);
    }

    private void handleDropItemEvent(final SkillService service, final DropItemEvent.Destruct event, final BlockSnapshot snapshot,
        final Player player) {

        final List<Item> items = event.getEntities()
            .stream()
            .filter(entity -> entity instanceof Item)
            .map(entity -> (Item) entity)
            .collect(Collectors.toList());

        if (items.isEmpty()) {
            return;
        }

        final SkillHolder holder = this.getSkillHolder(service, player);
        if (holder == null) {
            return;
        }

        final Map<SkillType, ChainIndex<ItemChain, ItemType>> eventChains = this.getItemChainsFor(event.getClass());
        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = holder.getSkills().values();
        final PositionKey position = new PositionKey(snapshot);

        for (final Item item : items) {
            final Collection<BuiltinResult> itemResults = this.handleItemStack(skills, eventChains, item.item().get().createStack());

            boolean denied = false;
            for (final BuiltinResult result : itemResults) {
                if (result.getType() == Result.Type.CANCELLED) {
                    denied = true;
                    this.sendDenyMessage(player, event.getCause(), result, DENY_MESSAGE_THROTTLE);
                }
            }

            if (denied) {
                // Remove the denied item, keep every other drop
                event.filterEntities(entity -> entity != item);
                continue;
            }

            this.queueRewards(event, player, RewardKind.DROP, itemResults, null, item, position);
        }
    }

    private Collection<BuiltinResult> processBlockSnapshotFor(final Cause cause, final Collection<Skill> skills,
        final Map<SkillType, ChainIndex<BlockChain, BlockType>> skillChains, final BlockSnapshot snapshot) {

        final List<BuiltinResult> results = new ArrayList<>();

        for (final Skill skill : skills) {

            final ChainIndex<BlockChain, BlockType> index = skillChains.get(skill.getSkillType());
            if (index == null) {
                continue;
            }

            // First registered chain matching the state wins
            BlockChain chain = null;
            for (final BlockChain candidate : index.candidatesFor(snapshot.getState().getType())) {
                if (candidate.matches(snapshot.getState())) {
                    chain = candidate;
                    break;
                }
            }

            if (chain == null) {
                continue;
            }

            final BuiltinResult.Builder builder = BuiltinResult.builder().skill(skill).type(Result.Type.SUCCESS).chain(chain);

            // Check owner first: if the creator filter excludes this block (e.g. a player placed it), the chain neither rewards nor gates
            // it. Level gates protect natural resources, not players' own builds.
            final TriFunction<Cause, Skill, BlockSnapshot, Boolean> owner = chain.owner;
            if (owner != null) {
                final Boolean ownerCheck = owner.apply(cause, skill, snapshot);

                if (ownerCheck != null && !ownerCheck) {
                    results.add(builder.build());
                    continue;
                }
            }

            // Check level second to set result cancellation
            if (chain.level != null && chain.level > skill.getCurrentLevel()) {
                builder.type(Result.Type.CANCELLED);
                results.add(builder.build());
                continue;
            }

            if (chain.xp != null) {
                builder.xp(chain.xp);
            }

            if (chain.economy != null) {
                final Double economy = chain.economy;
                skill.getSkillType().getEconomyFunction()
                    .ifPresent(func -> builder.money(func.getMoneyFor(skill.getCurrentLevel(), economy)));
            }

            results.add(builder.build());
        }

        return results;
    }

    private Collection<BuiltinResult> handleItemStack(final Collection<Skill> skills, final Map<SkillType, ChainIndex<ItemChain, ItemType>>
        skillChains, final ItemStack stack) {

        final List<BuiltinResult> results = new ArrayList<>();

        for (final Skill skill : skills) {

            final BuiltinResult.Builder builder = BuiltinResult.builder().skill(skill);

            // First registered chain matching the stack wins
            ItemChain chain = null;
            final ChainIndex<ItemChain, ItemType> index = skillChains.get(skill.getSkillType());
            if (index != null) {
                for (final ItemChain candidate : index.candidatesFor(stack.getType())) {
                    if (candidate.matches(stack)) {
                        chain = candidate;
                        break;
                    }
                }
            }

            builder.type(Result.Type.SUCCESS);

            if (chain != null) {

                builder.chain(chain);
                if (chain.level != null && chain.level > skill.getCurrentLevel()) {
                    builder.type(Result.Type.CANCELLED);
                } else {
                    if (chain.economy != null) {
                        final Double economy = chain.economy;
                        skill.getSkillType().getEconomyFunction()
                            .ifPresent(func -> builder.money(func.getMoneyFor(skill.getCurrentLevel(), economy)));
                    }

                    if (chain.xp != null) {
                        builder.xp(chain.xp);
                    }
                }
            }

            results.add(builder.build());
        }

        return results;
    }

    public BuiltinEventListener addEffectChain(final Class<? extends Event> clazz, final SkillType type, final EventFeedback feedback) {
        checkNotNull(type);
        checkNotNull(feedback);

        this.effectBuilders.computeIfAbsent(clazz, v -> new HashMap<>()).computeIfAbsent(type, v -> new ArrayList<>()).add(feedback);
        return this;
    }

    public BuiltinEventListener addMessageChain(final Class<? extends Event> clazz, final SkillType type, final EventFeedback feedback) {
        checkNotNull(type);
        checkNotNull(feedback);

        this.messageBuilders.computeIfAbsent(clazz, v -> new HashMap<>()).computeIfAbsent(type, v -> new ArrayList<>()).add(feedback);
        return this;
    }

    public BuiltinEventListener addBlockChain(final Class<? extends Event> clazz, final SkillType type, final BlockChain chain) {
        checkNotNull(type);
        checkNotNull(chain);

        if (chain.isInErrorState()) {
            this.logger.warn("Skipping misconfigured block chain for skill '{}' on '{}'.", type.getId(), clazz.getSimpleName());
            return this;
        }

        this.blockChains.computeIfAbsent(clazz, v -> new HashMap<>()).computeIfAbsent(type, v -> new ArrayList<>()).add(chain);
        this.blockChainIndex.clear();
        return this;
    }

    public BuiltinEventListener addItemChain(final Class<? extends Event> clazz, final SkillType type, final ItemChain chain) {
        checkNotNull(type);
        checkNotNull(chain);

        if (chain.isInErrorState()) {
            this.logger.warn("Skipping misconfigured item chain for skill '{}' on '{}'.", type.getId(), clazz.getSimpleName());
            return this;
        }

        this.itemChains.computeIfAbsent(clazz, v -> new HashMap<>()).computeIfAbsent(type, v -> new ArrayList<>()).add(chain);
        this.itemChainIndex.clear();
        return this;
    }

    private Map<SkillType, ChainIndex<BlockChain, BlockType>> getBlockChainsFor(final Class<?> eventClass) {
        Map<SkillType, ChainIndex<BlockChain, BlockType>> resolved = this.blockChainIndex.get(eventClass);
        if (resolved == null) {
            resolved = this.resolveChains(this.blockChains, eventClass, BlockChain::couldMatchType);
            this.blockChainIndex.put(eventClass, resolved);
        }
        return resolved;
    }

    private Map<SkillType, ChainIndex<ItemChain, ItemType>> getItemChainsFor(final Class<?> eventClass) {
        Map<SkillType, ChainIndex<ItemChain, ItemType>> resolved = this.itemChainIndex.get(eventClass);
        if (resolved == null) {
            resolved = this.resolveChains(this.itemChains, eventClass, ItemChain::couldMatchType);
            this.itemChainIndex.put(eventClass, resolved);
        }
        return resolved;
    }

    private <C extends Chain<C>, K> Map<SkillType, ChainIndex<C, K>> resolveChains(final Map<Class<? extends Event>, Map<SkillType, List<C>>>
        chains, final Class<?> eventClass, final BiPredicate<C, K> couldMatch) {

        final Map<SkillType, List<C>> eventChains = chains.entrySet()
            .stream()
            .filter(kv -> kv.getKey().isAssignableFrom(eventClass))
            .findAny()
            .map(Map.Entry::getValue)
            .orElse(null);

        if (eventChains == null || eventChains.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<SkillType, ChainIndex<C, K>> resolved = new HashMap<>();
        eventChains.forEach((type, list) -> resolved.put(type, new ChainIndex<>(list, couldMatch)));
        return resolved;
    }

    private void sendDenyMessage(final Player player, final Cause cause, final BuiltinResult result, final long throttle) {
        final Chain<?> chain = result.getChain().orElse(null);
        if (chain == null || chain.denyLevelRequired == null) {
            return;
        }

        final long stamp = System.currentTimeMillis();
        final Map<Chain<?>, Long> timers = this.denyTimers.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        final Long last = timers.get(chain);
        if (last != null && stamp - last < throttle) {
            return;
        }

        timers.put(chain, stamp);
        chain.denyLevelRequired.accept(cause, result.getSkill(), chain.level);
    }

    private void queueRewards(final Event event, final Player player, final RewardKind kind, final Collection<BuiltinResult> results,
        @Nullable final Transaction<BlockSnapshot> transaction, @Nullable final Item item, @Nullable final PositionKey position) {

        final List<BuiltinResult> rewarding = results
            .stream()
            .filter(result -> result.getType() == Result.Type.SUCCESS)
            .filter(result -> result.getXp().isPresent() || result.getMoney().isPresent())
            .collect(Collectors.toList());

        if (rewarding.isEmpty()) {
            return;
        }

        PendingRewards pending = this.pendingRewards.get(event);
        if (pending == null) {
            final long now = System.currentTimeMillis();
            if (this.pendingRewards.size() >= PENDING_REWARD_SWEEP_THRESHOLD) {
                // Safety net, POST should always consume these
                this.pendingRewards.values().removeIf(p -> now - p.created > PENDING_REWARD_TIMEOUT);
            }
            pending = new PendingRewards(player, now);
            this.pendingRewards.put(event, pending);
        }

        pending.rewards.add(new PendingReward(kind, rewarding, transaction, item, position));
    }

    private void payPendingRewards(final Event event) {
        if (this.pendingRewards.isEmpty()) {
            return;
        }

        final PendingRewards pending = this.pendingRewards.remove(event);
        if (pending == null) {
            return;
        }

        if (event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
            return;
        }

        final Player player = pending.player;
        if (!player.isOnline()) {
            return;
        }

        final long now = System.currentTimeMillis();
        final List<BuiltinResult> toPay = new ArrayList<>();

        for (final PendingReward reward : pending.rewards) {
            if (reward.transaction != null && !reward.transaction.isValid()) {
                continue;
            }

            if (reward.item != null && !this.containsEntity(event, reward.item)) {
                continue;
            }

            switch (reward.kind) {
                case PLACE:
                    if (!this.tryRecordPlacement(player, reward, now)) {
                        continue;
                    }
                    break;
                case BREAK:
                case DROP:
                    // A rewarded harvest of a position makes replanting it rewardable again
                    if (reward.position != null) {
                        this.clearPlacementHistory(reward.position);
                    }
                    break;
                default:
                    break;
            }

            toPay.addAll(reward.results);
        }

        if (toPay.isEmpty()) {
            return;
        }

        final Map<Skill, Double> totalXPGained = this.calculateXPGainedFor(toPay);
        final Map<Skill, BigDecimal> totalMoneyGained = this.calculateMoneyGainedFor(toPay);

        this.adjustXPAndMoney(event, player, totalXPGained, totalMoneyGained);
    }

    private boolean containsEntity(final Event event, final Entity entity) {
        if (!(event instanceof AffectEntityEvent)) {
            return true;
        }
        for (final Entity e : ((AffectEntityEvent) event).getEntities()) {
            if (e == entity) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return True if the placement may be rewarded (and records it), false if it is a repeat within the cooldown or converts a player
     * placed block.
     */
    private boolean tryRecordPlacement(final Player player, final PendingReward reward, final long now) {
        if (reward.position == null || reward.transaction == null) {
            return true;
        }

        final BlockSnapshot original = reward.transaction.getOriginal();
        if (original.getState().getType() != BlockTypes.AIR && original.getCreator().isPresent()) {
            return false;
        }

        final PlacementHistory history = this.placementHistory.computeIfAbsent(player.getUniqueId(), k -> new PlacementHistory());
        final Long last = history.get(reward.position);
        if (last != null && now - last < PLACEMENT_REWARD_COOLDOWN) {
            return false;
        }

        // Re-insert to move it to the end of the eviction order
        history.remove(reward.position);
        history.put(reward.position, now);
        return true;
    }

    private void clearPlacementHistory(final PositionKey position) {
        if (this.placementHistory.isEmpty()) {
            return;
        }
        for (final PlacementHistory history : this.placementHistory.values()) {
            history.remove(position);
        }
    }

    private void prunePlacementHistory(final long now) {
        final Iterator<PlacementHistory> iterator = this.placementHistory.values().iterator();
        while (iterator.hasNext()) {
            final PlacementHistory history = iterator.next();
            // Insertion ordered by time, oldest first
            final Iterator<Long> stamps = history.values().iterator();
            while (stamps.hasNext()) {
                if (now - stamps.next() >= PLACEMENT_REWARD_COOLDOWN) {
                    stamps.remove();
                } else {
                    break;
                }
            }
            if (history.isEmpty()) {
                iterator.remove();
            }
        }
    }

    private Map<Skill, Double> calculateXPGainedFor(final Collection<BuiltinResult> results) {
        final Map<Skill, Double> totalXPGained = new HashMap<>();

        for (final BuiltinResult successResult : results) {
            final Double xp = successResult.getXp().orElse(null);

            if (xp != null) {
                Double value = totalXPGained.get(successResult.getSkill());
                if (value != null) {
                    value = value + xp;
                } else {
                    value = xp;
                }

                totalXPGained.put(successResult.getSkill(), value);
            }
        }

        return totalXPGained;
    }

    private Map<Skill, BigDecimal> calculateMoneyGainedFor(final Collection<BuiltinResult> results) {
        final Map<Skill, BigDecimal> totalMoneyGained = new HashMap<>();

        for (final BuiltinResult successResult : results) {
            final BigDecimal money = successResult.getMoney().orElse(null);
            if (money != null) {
                BigDecimal value = totalMoneyGained.get(successResult.getSkill());
                if (value != null) {
                    value = value.add(money);
                } else {
                    value = money;
                }

                totalMoneyGained.put(successResult.getSkill(), value);
            }
        }

        return totalMoneyGained;
    }

    /**
     * @return True if any chain registered for the event class matches the stack, for any skill
     */
    boolean hasItemChain(final Class<?> eventClass, final ItemStack stack) {
        for (final ChainIndex<ItemChain, ItemType> index : this.getItemChainsFor(eventClass).values()) {
            for (final ItemChain chain : index.candidatesFor(stack.getType())) {
                if (chain.matches(stack)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Pays the holder's skills the rewards of the chains registered for the event class that match the stack, {@code count} times.
     * Chains above the skill's level pay nothing. Used for rewards that are not tied to an event in progress (smelting).
     *
     * @return The XP paid per skill
     */
    Map<Skill, Double> rewardItem(@Nullable final Event event, final Player player, final SkillHolder holder, final Class<?> eventClass,
        final ItemStack stack, final int count) {

        final Map<SkillType, ChainIndex<ItemChain, ItemType>> eventChains = this.getItemChainsFor(eventClass);
        if (eventChains.isEmpty() || count <= 0) {
            return Collections.emptyMap();
        }

        final List<BuiltinResult> rewarding = this.handleItemStack(holder.getSkills().values(), eventChains, stack)
            .stream()
            .filter(result -> result.getType() == Result.Type.SUCCESS)
            .collect(Collectors.toList());

        final Map<Skill, Double> totalXPGained = this.calculateXPGainedFor(rewarding);
        final Map<Skill, BigDecimal> totalMoneyGained = this.calculateMoneyGainedFor(rewarding);
        if (totalXPGained.isEmpty() && totalMoneyGained.isEmpty()) {
            return Collections.emptyMap();
        }

        if (count > 1) {
            totalXPGained.replaceAll((skill, xp) -> xp * count);
            totalMoneyGained.replaceAll((skill, money) -> money.multiply(BigDecimal.valueOf(count)));
        }

        final Map<Skill, Double> paid = new HashMap<>(totalXPGained);
        this.adjustXPAndMoney(event, player, totalXPGained, totalMoneyGained);
        return paid;
    }

    private void adjustXPAndMoney(@Nullable final Event event, final Player player, final Map<Skill, Double> totalXPGained,
        final Map<Skill, BigDecimal> totalMoneyGained) {

        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            // The player is pushed too, for rewards paid outside of the player's own events (smelting)
            frame.pushCause(player);
            if (event != null) {
                frame.pushCause(event);
            }

            for (final Map.Entry<Skill, Double> entry : totalXPGained.entrySet()) {
                final Skill skill = entry.getKey();
                final Double xpValue = entry.getValue();

                final ExperienceResult result = skill.addExperience(xpValue);

                if (result.getType() == Result.Type.CANCELLED) {
                    totalMoneyGained.remove(entry.getKey());
                }
            }

            if (totalMoneyGained.isEmpty()) {
                return;
            }

            final EconomyService economyService = this.serviceManager.provide(EconomyService.class).orElse(null);

            final UniqueAccount account = economyService == null ? null : economyService.getOrCreateAccount(player.getUniqueId()).orElse(null);

            if (account == null) {
                return;
            }

            for (final Map.Entry<Skill, BigDecimal> entry : totalMoneyGained.entrySet()) {
                final BigDecimal moneyValue = totalMoneyGained.get(entry.getKey());

                account.deposit(economyService.getDefaultCurrency(), moneyValue, frame.getCurrentCause());
            }
        }
    }

    /**
     * Chains of one skill/event in registration order, with the candidate chains per block/item type cached so lookups don't scan
     * every chain (and every queried state) on each event. Candidates keep registration order, so first-match-wins is preserved.
     */
    private static final class ChainIndex<C extends Chain<C>, K> {

        private final List<C> chains;
        private final BiPredicate<C, K> couldMatch;
        private final Map<K, List<C>> candidates = new HashMap<>();

        ChainIndex(final List<C> chains, final BiPredicate<C, K> couldMatch) {
            this.chains = chains;
            this.couldMatch = couldMatch;
        }

        List<C> candidatesFor(final K key) {
            List<C> list = this.candidates.get(key);
            if (list == null) {
                list = new ArrayList<>();
                for (final C chain : this.chains) {
                    if (this.couldMatch.test(chain, key)) {
                        list.add(chain);
                    }
                }
                list = list.isEmpty() ? Collections.emptyList() : list;
                this.candidates.put(key, list);
            }
            return list;
        }
    }

    private enum RewardKind {
        PLACE,
        BREAK,
        DROP,
        OTHER
    }

    private static final class PendingReward {

        final RewardKind kind;
        final List<BuiltinResult> results;
        @Nullable final Transaction<BlockSnapshot> transaction;
        @Nullable final Item item;
        @Nullable final PositionKey position;

        PendingReward(final RewardKind kind, final List<BuiltinResult> results, @Nullable final Transaction<BlockSnapshot> transaction,
            @Nullable final Item item, @Nullable final PositionKey position) {
            this.kind = kind;
            this.results = results;
            this.transaction = transaction;
            this.item = item;
            this.position = position;
        }
    }

    private static final class PendingRewards {

        final Player player;
        final long created;
        final List<PendingReward> rewards = new ArrayList<>();

        PendingRewards(final Player player, final long created) {
            this.player = player;
            this.created = created;
        }
    }

    /**
     * Rewarded placement positions of one player, insertion (= time) ordered and size bounded.
     */
    private static final class PlacementHistory extends LinkedHashMap<PositionKey, Long> {

        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(final Map.Entry<PositionKey, Long> eldest) {
            return this.size() > PLACEMENT_HISTORY_MAX_PER_PLAYER;
        }
    }

    private static final class PositionKey {

        private final UUID world;
        private final int x, y, z;

        PositionKey(final BlockSnapshot snapshot) {
            final Vector3i position = snapshot.getPosition();
            this.world = snapshot.getWorldUniqueId();
            this.x = position.getX();
            this.y = position.getY();
            this.z = position.getZ();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PositionKey)) {
                return false;
            }
            final PositionKey other = (PositionKey) o;
            return this.x == other.x && this.y == other.y && this.z == other.z && this.world.equals(other.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.world, this.x, this.y, this.z);
        }
    }
}
