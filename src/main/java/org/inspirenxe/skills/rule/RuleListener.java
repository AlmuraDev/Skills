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

import org.apache.logging.log4j.Logger;
import org.inspirenxe.skills.api.ExperienceResult;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.Skills;
import org.inspirenxe.skills.feedback.Feedback;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.AffectEntityEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.math.vector.Vector3i;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Applies the skills' rules to game events.
 *
 * <p><b>Two phase processing.</b> Rules are evaluated at {@link Order#EARLY}: level gates deny the action there (cancelling the event,
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
 *     until {@link #PLACEMENT_REWARD_COOLDOWN} has passed. Only a <i>rewarded</i> harvest of that position (a break or drop that paid,
 *     i.e. a mature crop) clears the mark, so normal plant, grow, harvest, replant farming keeps paying.</li>
 *     <li>Memory is bounded: at most {@link #PLACEMENT_HISTORY_MAX_PER_PLAYER} positions per player (oldest evicted first), expired
 *     marks are pruned whenever players join or quit. A quitting player's marks are kept until they expire.</li>
 * </ul>
 */
public final class RuleListener {

    private static final long PLACEMENT_REWARD_COOLDOWN = TimeUnit.MINUTES.toMillis(10);
    private static final int PLACEMENT_HISTORY_MAX_PER_PLAYER = 4096;
    private static final long DENY_MESSAGE_THROTTLE = 5000;
    private static final long INTERACT_DENY_MESSAGE_THROTTLE = 3500;
    private static final long PENDING_REWARD_TIMEOUT = TimeUnit.SECONDS.toMillis(30);
    private static final int PENDING_REWARD_SWEEP_THRESHOLD = 64;

    private final Logger logger;
    private final Feedback feedback;
    private RuleBook rules = new RuleBook();
    // Event (identity) -> rewards computed at EARLY, paid at POST
    private final Map<Event, PendingRewards> pendingRewards = new IdentityHashMap<>();
    // Player -> rewarded placement positions
    private final Map<UUID, PlacementHistory> placementHistory = new HashMap<>();

    public RuleListener(final Logger logger, final Feedback feedback) {
        this.logger = logger;
        this.feedback = feedback;
    }

    public RuleBook rules() {
        return this.rules;
    }

    public void setRules(final RuleBook rules) {
        this.rules = rules;
        this.pendingRewards.clear();
        this.feedback.clearDenyTimers();
    }

    @Listener
    public void onJoin(final ServerSideConnectionEvent.Join event) {
        this.prunePlacementHistory(System.currentTimeMillis());
    }

    @Listener
    public void onLeave(final ServerSideConnectionEvent.Leave event) {
        this.feedback.forget(event.player().uniqueId());
        // The player's own placement history is intentionally kept until it expires, so re-logging doesn't reset the cooldown.
        this.prunePlacementHistory(System.currentTimeMillis());
    }

    // Phase one: gates and rewards to pay

    @Listener(order = Order.EARLY)
    public void onChangeBlock(final ChangeBlockEvent.All event, @First final ServerPlayer player) {
        final Cause cause = event.cause();
        // Blocks that fall, pistons, block ticks etc. are not the player's own doing; neither are changes made by commands
        // (/fill, /setblock, WorldEdit), which Sponge credits to the player who ran them
        if (cause.containsType(FallingBlock.class) || RuleListener.byCommand(event)) {
            return;
        }
        final boolean placedByPlayer = cause.root() instanceof ServerPlayer && !cause.containsType(LocatableBlock.class);
        final SkillHolder holder = this.holder(player);
        if (holder == null) {
            return;
        }
        final Operation breakOp = Operations.BREAK.get();
        final Operation placeOp = Operations.PLACE.get();
        for (final BlockTransaction transaction : event.transactions()) {
            if (!transaction.isValid()) {
                continue;
            }
            final Operation operation = transaction.operation();
            final Trigger trigger;
            final BlockSnapshot snapshot;
            if (operation.equals(breakOp)) {
                trigger = Trigger.BREAK;
                snapshot = transaction.original();
            } else if (operation.equals(placeOp) && placedByPlayer) {
                trigger = Trigger.PLACE;
                snapshot = transaction.finalReplacement();
            } else {
                continue;
            }
            if (!this.rules.has(trigger)) {
                continue;
            }
            final List<Reward> rewards = new ArrayList<>();
            if (this.evaluateBlock(player, holder, trigger, snapshot, rewards, RuleListener.DENY_MESSAGE_THROTTLE)) {
                // Nothing on this block is rewarded, not even skills whose gate passed
                transaction.setValid(false);
                continue;
            }
            this.queue(event, player, trigger == Trigger.PLACE ? RewardKind.PLACE : RewardKind.BREAK, rewards, transaction, null,
                    new PositionKey(snapshot.world().asString(), snapshot.position()));
        }
    }

    @Listener(order = Order.EARLY)
    public void onInteractItem(final InteractItemEvent.Secondary event, @Root final ServerPlayer player) {
        if (this.useTool(event, player, event.itemStack().type())) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.EARLY)
    public void onInteractBlockPrimary(final InteractBlockEvent.Primary.Start event, @Root final ServerPlayer player) {
        // Hitting/breaking a block uses the tool in the main hand
        final ItemStack held = player.itemInHand(HandTypes.MAIN_HAND);
        if (!held.isEmpty() && this.useTool(event, player, held.type())) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.EARLY)
    public void onInteractBlockSecondary(final InteractBlockEvent.Secondary.Pre event, @Root final ServerPlayer player) {
        // Only deny the USE of a too high level tool (e.g. hoeing), never the block interaction itself, so opening chests, doors,
        // etc. while holding such a tool still works.
        final HandType hand = event.context().get(EventContextKeys.USED_HAND).orElse(HandTypes.MAIN_HAND.get());
        final ItemStack held = player.itemInHand(hand);
        if (!held.isEmpty() && this.useTool(event, player, held.type())) {
            event.setUseItemResult(Tristate.FALSE);
        }
        if (!this.rules.has(Trigger.INTERACT_BLOCK)) {
            return;
        }
        final SkillHolder holder = this.holder(player);
        if (holder == null) {
            return;
        }
        final List<Reward> rewards = new ArrayList<>();
        if (this.evaluateBlock(player, holder, Trigger.INTERACT_BLOCK, event.block(), rewards, RuleListener.INTERACT_DENY_MESSAGE_THROTTLE)) {
            event.setCancelled(true);
            return;
        }
        this.queue(event, player, RewardKind.OTHER, rewards, null, null, null);
    }

    @Listener(order = Order.EARLY)
    public void onCraft(final CraftItemEvent.Craft event, @Root final ServerPlayer player) {
        if (!this.rules.has(Trigger.CRAFT)) {
            return;
        }
        final SkillHolder holder = this.holder(player);
        if (holder == null) {
            return;
        }
        // The recipe's own result, so a stack changed by other plugins still counts as what was crafted
        final ItemType crafted = event.recipe()
                .map(recipe -> recipe.exemplaryResult().type())
                .orElse(event.crafted().type());
        final List<Reward> rewards = new ArrayList<>();
        if (this.evaluateItem(player, holder, Trigger.CRAFT, crafted, rewards, RuleListener.DENY_MESSAGE_THROTTLE)) {
            event.setCancelled(true);
            return;
        }
        this.queue(event, player, RewardKind.OTHER, rewards, null, null, null);
    }

    @Listener(order = Order.EARLY)
    public void onDropFromBlock(final DropItemEvent.Destruct event, @First final ServerPlayer player) {
        if (!this.rules.has(Trigger.DROP) || RuleListener.byCommand(event)) {
            return;
        }
        final BlockSnapshot block = event.cause().first(BlockSnapshot.class)
                .or(() -> event.context().get(EventContextKeys.BLOCK_TARGET))
                .orElse(null);
        // Only drops of a broken block; blocks with a block entity (chests etc.) drop their contents, which is no harvest
        if (block == null || block.createArchetype().isPresent()) {
            return;
        }
        final SkillHolder holder = this.holder(player);
        if (holder == null) {
            return;
        }
        final PositionKey position = new PositionKey(block.world().asString(), block.position());
        for (final Entity entity : new ArrayList<>(event.entities())) {
            if (!(entity instanceof Item item)) {
                continue;
            }
            final ItemStackSnapshot stack = item.item().get();
            final List<Reward> rewards = new ArrayList<>();
            if (this.evaluateItem(player, holder, Trigger.DROP, stack.type(), rewards, RuleListener.DENY_MESSAGE_THROTTLE)) {
                // Remove the denied item, keep every other drop
                event.filterEntities(e -> e != item);
                continue;
            }
            this.queue(event, player, RewardKind.DROP, rewards, null, item, position);
        }
    }

    // Phase two: pay once the final outcome of the event is known. UNDEFINED so rewards of cancelled events are discarded too.

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.UNDEFINED)
    public void onChangeBlockPost(final ChangeBlockEvent.All event) {
        this.pay(event);
    }

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.UNDEFINED)
    public void onInteractItemPost(final InteractItemEvent.Secondary event) {
        this.pay(event);
    }

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.UNDEFINED)
    public void onInteractBlockPrimaryPost(final InteractBlockEvent.Primary.Start event) {
        this.pay(event);
    }

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.UNDEFINED)
    public void onInteractBlockSecondaryPost(final InteractBlockEvent.Secondary.Pre event) {
        this.pay(event);
    }

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.UNDEFINED)
    public void onCraftPost(final CraftItemEvent.Craft event) {
        this.pay(event);
    }

    @Listener(order = Order.POST)
    @IsCancelled(Tristate.UNDEFINED)
    public void onDropFromBlockPost(final DropItemEvent.Destruct event) {
        this.pay(event);
    }

    /**
     * @return True if the event happened while a command ran; commands neither earn rewards nor are level gated
     */
    private static boolean byCommand(final Event event) {
        return event.context().containsKey(EventContextKeys.COMMAND);
    }

    // Evaluation

    /**
     * @return True if a level gate denied using the item; rewards are queued only when not denied
     */
    private boolean useTool(final Event event, final ServerPlayer player, final ItemType type) {
        if (!this.rules.has(Trigger.USE_TOOL)) {
            return false;
        }
        final SkillHolder holder = this.holder(player);
        if (holder == null) {
            return false;
        }
        final List<Reward> rewards = new ArrayList<>();
        if (this.evaluateItem(player, holder, Trigger.USE_TOOL, type, rewards, RuleListener.INTERACT_DENY_MESSAGE_THROTTLE)) {
            return true;
        }
        this.queue(event, player, RewardKind.OTHER, rewards, null, null, null);
        return false;
    }

    /**
     * Evaluates the first matching rule of every skill against the block.
     *
     * @return True if any skill's level gate denied
     */
    private boolean evaluateBlock(final ServerPlayer player, final SkillHolder holder, final Trigger trigger, final BlockSnapshot snapshot,
            final List<Reward> rewards, final long throttle) {
        boolean denied = false;
        final UUID creator = snapshot.creator().orElse(null);
        for (final Skill skill : holder.skills().values()) {
            final Rule rule = this.rules.find(trigger, skill.type(), snapshot.state()).orElse(null);
            if (rule == null) {
                continue;
            }
            // The creator filter first: a block the rule does not apply to (e.g. a player placed it) is neither rewarded nor gated
            if (!rule.creator().test(player.uniqueId(), creator)) {
                continue;
            }
            if (rule.level() != null && rule.level() > skill.level()) {
                denied = true;
                this.feedback.deny(player, rule, skill.type(), rule.deny(), rule.level(), throttle);
                continue;
            }
            this.addReward(skill, rule, rewards);
        }
        return denied;
    }

    /**
     * Evaluates the first matching rule of every skill against the item.
     *
     * @return True if any skill's level gate denied
     */
    private boolean evaluateItem(final ServerPlayer player, final SkillHolder holder, final Trigger trigger, final ItemType type,
            final List<Reward> rewards, final long throttle) {
        boolean denied = false;
        for (final Skill skill : holder.skills().values()) {
            final Rule rule = this.rules.find(trigger, skill.type(), type).orElse(null);
            if (rule == null) {
                continue;
            }
            if (rule.level() != null && rule.level() > skill.level()) {
                denied = true;
                this.feedback.deny(player, rule, skill.type(), rule.deny(), rule.level(), throttle);
                continue;
            }
            this.addReward(skill, rule, rewards);
        }
        return denied;
    }

    private void addReward(final Skill skill, final Rule rule, final List<Reward> rewards) {
        if (!rule.rewards()) {
            return;
        }
        BigDecimal money = null;
        if (rule.money() != null) {
            final double modifier = rule.money();
            money = skill.type().economyFunction().map(function -> function.moneyFor(skill.level(), modifier)).orElse(null);
        }
        rewards.add(new Reward(skill, rule.xp() == null ? 0 : rule.xp(), money));
    }

    /**
     * Pays the holder's skills the rewards of the rules of the trigger that match the item, {@code count} times. Rules above the
     * skill's level pay nothing. Used for rewards that are not tied to an event in progress (smelting).
     *
     * @return The XP paid per skill
     */
    public Map<Skill, Double> rewardItem(final Event event, final ServerPlayer player, final SkillHolder holder, final Trigger trigger,
            final ItemType type, final int count) {
        if (count <= 0) {
            return Collections.emptyMap();
        }
        final List<Reward> rewards = new ArrayList<>();
        for (final Skill skill : holder.skills().values()) {
            final Rule rule = this.rules.find(trigger, skill.type(), type).orElse(null);
            if (rule != null && (rule.level() == null || rule.level() <= skill.level())) {
                this.addReward(skill, rule, rewards);
            }
        }
        if (rewards.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<Skill, Double> xp = new LinkedHashMap<>();
        final Map<Skill, BigDecimal> money = new LinkedHashMap<>();
        RuleListener.sum(rewards, xp, money);
        if (count > 1) {
            xp.replaceAll((skill, value) -> value * count);
            money.replaceAll((skill, value) -> value.multiply(BigDecimal.valueOf(count)));
        }
        final Map<Skill, Double> paid = new LinkedHashMap<>(xp);
        this.payOut(event, player, xp, money);
        return paid;
    }

    // Pending rewards

    private void queue(final Event event, final ServerPlayer player, final RewardKind kind, final List<Reward> rewards,
            final BlockTransaction transaction, final Item item, final PositionKey position) {
        if (rewards.isEmpty()) {
            return;
        }
        PendingRewards pending = this.pendingRewards.get(event);
        if (pending == null) {
            final long now = System.currentTimeMillis();
            if (this.pendingRewards.size() >= RuleListener.PENDING_REWARD_SWEEP_THRESHOLD) {
                // Safety net, POST should always consume these
                this.pendingRewards.values().removeIf(p -> now - p.created > RuleListener.PENDING_REWARD_TIMEOUT);
            }
            pending = new PendingRewards(player.uniqueId(), now);
            this.pendingRewards.put(event, pending);
        }
        pending.rewards.add(new PendingReward(kind, rewards, transaction, item, position));
    }

    private void pay(final Event event) {
        if (this.pendingRewards.isEmpty()) {
            return;
        }
        final PendingRewards pending = this.pendingRewards.remove(event);
        if (pending == null || event instanceof Cancellable cancellable && cancellable.isCancelled()) {
            return;
        }
        final ServerPlayer player = Sponge.server().player(pending.player).orElse(null);
        if (player == null) {
            return;
        }
        final long now = System.currentTimeMillis();
        final List<Reward> toPay = new ArrayList<>();
        for (final PendingReward reward : pending.rewards) {
            if (reward.transaction != null && !reward.transaction.isValid()) {
                continue;
            }
            if (reward.item != null && !RuleListener.containsEntity(event, reward.item)) {
                continue;
            }
            switch (reward.kind) {
                case PLACE -> {
                    if (!this.tryRecordPlacement(player, reward, now)) {
                        continue;
                    }
                }
                // A rewarded harvest of a position makes replanting it rewardable again
                case BREAK, DROP -> {
                    if (reward.position != null) {
                        this.clearPlacementHistory(reward.position);
                    }
                }
                default -> {
                }
            }
            toPay.addAll(reward.rewards);
        }
        if (toPay.isEmpty()) {
            return;
        }
        final Map<Skill, Double> xp = new LinkedHashMap<>();
        final Map<Skill, BigDecimal> money = new LinkedHashMap<>();
        RuleListener.sum(toPay, xp, money);
        this.payOut(event, player, xp, money);
    }

    private static void sum(final List<Reward> rewards, final Map<Skill, Double> xp, final Map<Skill, BigDecimal> money) {
        for (final Reward reward : rewards) {
            if (reward.xp != 0) {
                xp.merge(reward.skill, reward.xp, Double::sum);
            }
            if (reward.money != null && reward.money.signum() != 0) {
                money.merge(reward.skill, reward.money, BigDecimal::add);
            }
        }
    }

    private void payOut(final Event event, final ServerPlayer player, final Map<Skill, Double> xp, final Map<Skill, BigDecimal> money) {
        final CauseStackManager causes = Sponge.server().causeStackManager();
        try (CauseStackManager.StackFrame frame = causes.pushCauseFrame()) {
            if (event != null) {
                frame.pushCause(event);
            }
            // The player goes on top, so rewards paid outside of the player's own events (smelting) still name them
            frame.pushCause(player);
            for (final Map.Entry<Skill, Double> entry : xp.entrySet()) {
                final ExperienceResult result = entry.getKey().addExperience(entry.getValue());
                if (result.type() == ExperienceResult.Type.CANCELLED) {
                    money.remove(entry.getKey());
                }
            }
            if (money.isEmpty()) {
                return;
            }
            final EconomyService economy = Sponge.server().serviceProvider().economyService().orElse(null);
            if (economy == null) {
                return;
            }
            final UniqueAccount account = economy.findOrCreateAccount(player.uniqueId()).orElse(null);
            if (account == null) {
                return;
            }
            for (final BigDecimal amount : money.values()) {
                if (amount.signum() > 0) {
                    account.deposit(economy.defaultCurrency(), amount, frame.currentCause());
                }
            }
        } catch (final RuntimeException e) {
            this.logger.error("Failed to pay the skill rewards of '{}'!", player.name(), e);
        }
    }

    private static boolean containsEntity(final Event event, final Entity entity) {
        if (!(event instanceof AffectEntityEvent affect)) {
            return true;
        }
        for (final Entity e : affect.entities()) {
            if (e == entity) {
                return true;
            }
        }
        return false;
    }

    // Placement history

    /**
     * @return True if the placement may be rewarded (and records it), false if it repeats within the cooldown or converts a player
     * placed block
     */
    private boolean tryRecordPlacement(final ServerPlayer player, final PendingReward reward, final long now) {
        if (reward.position == null || reward.transaction == null) {
            return true;
        }
        final BlockSnapshot original = reward.transaction.original();
        if (!original.state().type().equals(BlockTypes.AIR.get()) && original.creator().isPresent()) {
            return false;
        }
        final PlacementHistory history = this.placementHistory.computeIfAbsent(player.uniqueId(), k -> new PlacementHistory());
        final Long last = history.get(reward.position);
        if (last != null && now - last < RuleListener.PLACEMENT_REWARD_COOLDOWN) {
            return false;
        }
        // Re-insert to move it to the end of the eviction order
        history.remove(reward.position);
        history.put(reward.position, now);
        return true;
    }

    private void clearPlacementHistory(final PositionKey position) {
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
                if (now - stamps.next() >= RuleListener.PLACEMENT_REWARD_COOLDOWN) {
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

    /**
     * @return The player's skills in their world, or null if the player earns nothing (creative mode, skills not loaded)
     */
    private SkillHolder holder(final ServerPlayer player) {
        if (!Skills.isAvailable() || player.gameMode().get().equals(GameModes.CREATIVE.get())) {
            return null;
        }
        return Skills.service().containerFor(player.world().key()).flatMap(container -> container.holder(player.uniqueId())).orElse(null);
    }

    private enum RewardKind {
        PLACE,
        BREAK,
        DROP,
        OTHER
    }

    private record Reward(Skill skill, double xp, BigDecimal money) {
    }

    private record PendingReward(RewardKind kind, List<Reward> rewards, BlockTransaction transaction, Item item, PositionKey position) {
    }

    private static final class PendingRewards {

        final UUID player;
        final long created;
        final List<PendingReward> rewards = new ArrayList<>();

        PendingRewards(final UUID player, final long created) {
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
            return this.size() > RuleListener.PLACEMENT_HISTORY_MAX_PER_PLAYER;
        }
    }

    private record PositionKey(String world, Vector3i position) {
    }
}
