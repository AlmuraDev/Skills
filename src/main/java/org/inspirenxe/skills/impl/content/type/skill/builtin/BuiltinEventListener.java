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
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
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
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.world.LocatableBlock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// TODO Plan B. Move to scripting engine later.

@Singleton
public final class BuiltinEventListener implements Witness {

    private final Logger logger;
    private final ServiceManager serviceManager;
    private final Map<Class<? extends Event>, Map<SkillType, List<EventFeedback>>> messageBuilders = new HashMap<>();
    private final Map<Class<? extends Event>, Map<SkillType, List<EventFeedback>>> effectBuilders = new HashMap<>();
    private final Map<Class<? extends Event>, Map<SkillType, List<BlockChain>>> blockChains = new HashMap<>();
    private final Map<Class<? extends Event>, Map<SkillType, List<ItemChain>>> itemChains = new HashMap<>();

    private final Map<Chain<?>, Long> denyTimers = new HashMap<>();

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

    @Listener(order = Order.EARLY)
    public void onChangeBlockBreak(final ChangeBlockEvent.Break event, @First final Player player) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        // Patch getting XP for blocks that fall
        if (event.getCause().containsType(FallingBlock.class)) {
            return;
        }

        this.handleChangeBlock(service, event, player, true);
    }

    @Listener(order = Order.EARLY)
    public void onChangeBlockModify(final ChangeBlockEvent.Modify event, @Root final Player player) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        this.handleChangeBlock(service, event, player, false);
    }

    @Listener(order = Order.EARLY)
    public void onChangeBlockPlace(final ChangeBlockEvent.Place event, @Root final Player player) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        this.handleChangeBlock(service, event, player, false);
    }

    @Listener(order = Order.EARLY)
    public void onInteractItem(final InteractItemEvent event, @Root final Player player) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        this.handleInteractItem(service, event, event.getClass(), event.getItemStack().createStack(), player);
    }

    @Listener(order = Order.EARLY)
    public void onInteractBlock(final InteractBlockEvent event, @Root final Player player) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        final boolean isRightClick = event instanceof InteractBlockEvent.Secondary;

        // If the Player has an item in their hand, give that priority and allow it to cancel the block interaction (main hand -> offhand -> block).
        // Needed for farming tools
        ItemStack itemStack = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
        if (itemStack != null) {
            this.handleInteractItem(service, event, !isRightClick ? InteractItemEvent.Primary.MainHand.class : InteractItemEvent.Secondary.MainHand.class, itemStack, player);
        }
        if (event.isCancelled()) {
            return;
        }

        itemStack = player.getItemInHand(HandTypes.OFF_HAND).orElse(null);
        if (itemStack != null) {
            this.handleInteractItem(service, event, !isRightClick ? InteractItemEvent.Primary.OffHand.class : InteractItemEvent.Secondary.OffHand.class, itemStack, player);
        }
        if (event.isCancelled()) {
            return;
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
        this.handleDropItemEvent(service, event, player);
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
        this.denyTimers.clear();

        MiningRegistar.configure();
        CraftingRegistar.configure();
        FarmingRegistar.configure();
        WoodcuttingRegistar.configure();
        DiggerRegistar.configure();
    }

    private void handleChangeBlock(final SkillService service, final ChangeBlockEvent event, final Player player, final boolean originalState) {
        if (player.gameMode().get() == GameModes.CREATIVE) {
            return;
        }

        final SkillHolderContainer worldContainer = service.getContainer(player.getWorld().getUniqueId()).orElse(null);
        if (worldContainer == null) {
            return;
        }

        final SkillHolderContainer container = service.getParentContainer(worldContainer).orElse(null);
        if (container == null) {
            return;
        }

        final SkillHolder skillHolder = container.getHolder(player.getUniqueId()).orElse(null);

        if (skillHolder == null) {
            return;
        }

        final Map<SkillType, List<BlockChain>> eventChains = this.blockChains.entrySet()
            .stream()
            .filter(kv -> kv.getKey().isAssignableFrom(event.getClass()))
            .findAny()
            .map(Map.Entry::getValue)
            .orElse(new HashMap<>());

        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = skillHolder.getSkills().values();

        final Set<Map.Entry<SkillType, List<BlockChain>>> skillChains = eventChains.entrySet()
            .stream()
            .filter(kv -> skills.stream().anyMatch(v -> v.getSkillType() == kv.getKey()))
            .collect(Collectors.toSet());

        final List<BuiltinResult> successResults = new ArrayList<>();

        for (final Transaction<BlockSnapshot> transaction : event.getTransactions()) {

            final BlockSnapshot snapshot = originalState ? transaction.getOriginal() : transaction.getFinal();

            final Collection<BuiltinResult> transactionResults = this.processBlockSnapshotFor(event.getCause(), skills, skillChains, snapshot);

            final List<BuiltinResult> cancelledResults = transactionResults
                .stream()
                .filter(result -> result.getType() == Result.Type.CANCELLED)
                .collect(Collectors.toList());

            for (final BuiltinResult cancelledResult : cancelledResults) {
                transaction.setValid(false);

                final Chain<?> chain = cancelledResult.getChain().orElse(null);
                if (chain != null && chain.denyLevelRequired != null) {
                    final long stamp = System.currentTimeMillis();
                    final Long timer = this.denyTimers.computeIfAbsent(chain, (c) -> stamp - 5000);

                    final long diff = stamp - timer;

                    if (diff < 5000) {
                        continue;
                    }

                    this.denyTimers.put(chain, stamp);
                    chain.denyLevelRequired.accept(event.getCause(), cancelledResult.getSkill(), chain.level);
                }
            }

            successResults.addAll(transactionResults
                .stream()
                .filter(result -> result.getType() == Result.Type.SUCCESS)
                .collect(Collectors.toList())
            );
        }

        final Map<Skill, Double> totalXPGained = this.calculateXPGainedFor(successResults);
        final Map<Skill, BigDecimal> totalMoneyGained = this.calculateMoneyGainedFor(successResults);

        this.adjustXPAndMoney(event, player, totalXPGained, totalMoneyGained);
    }

    private void handleInteractItem(final SkillService service, final Event event, final Class<? extends Event> classFilter, final ItemStack eventStack, final Player player) {
        if (player.gameMode().get() == GameModes.CREATIVE) {
            return;
        }

        final SkillHolderContainer worldContainer = service.getContainer(player.getWorld().getUniqueId()).orElse(null);
        if (worldContainer == null) {
            return;
        }

        final SkillHolderContainer container = service.getParentContainer(worldContainer).orElse(null);
        if (container == null) {
            return;
        }

        final SkillHolder skillHolder = container.getHolder(player.getUniqueId()).orElse(null);

        if (skillHolder == null) {
            return;
        }

        final Map<SkillType, List<ItemChain>> eventChains = this.itemChains.entrySet()
            .stream()
            .filter(kv -> kv.getKey().isAssignableFrom(classFilter))
            .findAny()
            .map(Map.Entry::getValue)
            .orElse(new HashMap<>());

        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = skillHolder.getSkills().values();

        final Set<Map.Entry<SkillType, List<ItemChain>>> skillChains = eventChains.entrySet()
            .stream()
            .filter(kv -> skills
                .stream()
                .anyMatch(v -> v.getSkillType() == kv.getKey()))
            .collect(Collectors.toSet());

        if (skillChains.isEmpty()) {
            return;
        }

        final Collection<BuiltinResult> results = this.handleItemStack(skills, skillChains, eventStack);

        final List<BuiltinResult> cancelledResults = results
            .stream()
            .filter(result -> result.getType() == Result.Type.CANCELLED)
            .collect(Collectors.toList());

        for (final BuiltinResult cancelledResult : cancelledResults) {
            if (event instanceof Cancellable) {
                ((Cancellable) event).setCancelled(true);
            }

            final Chain<?> chain = cancelledResult.getChain().orElse(null);
            if (chain != null && chain.denyLevelRequired != null) {
                final long stamp = System.currentTimeMillis();
                final Long timer = this.denyTimers.computeIfAbsent(chain, (c) -> stamp - 5000);

                final long diff = stamp - timer;

                if (diff < 5000) {
                    continue;
                }

                this.denyTimers.put(chain, stamp);
                chain.denyLevelRequired.accept(event.getCause(), cancelledResult.getSkill(), chain.level);
            }
        }

        if (event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
            return;
        }

        final List<BuiltinResult> successResults = results
            .stream()
            .filter(result -> result.getType() == Result.Type.SUCCESS)
            .collect(Collectors.toList());

        final Map<Skill, Double> totalXPGained = this.calculateXPGainedFor(successResults);
        final Map<Skill, BigDecimal> totalMoneyGained = this.calculateMoneyGainedFor(successResults);

        this.adjustXPAndMoney(event, player, totalXPGained, totalMoneyGained);
    }

    private void handleInteractBlock(final SkillService service, final InteractBlockEvent event, final Player player) {
        if (player.gameMode().get() == GameModes.CREATIVE) {
            return;
        }

        final SkillHolderContainer worldContainer = service.getContainer(player.getWorld().getUniqueId()).orElse(null);
        if (worldContainer == null) {
            return;
        }

        final SkillHolderContainer container = service.getParentContainer(worldContainer).orElse(null);
        if (container == null) {
            return;
        }

        final SkillHolder holder = container.getHolder(player.getUniqueId()).orElse(null);

        if (holder == null) {
            return;
        }

        final Map<SkillType, List<BlockChain>> eventChains = this.blockChains.entrySet()
            .stream()
            .filter(kv -> kv.getKey().isAssignableFrom(event.getClass()))
            .findAny()
            .map(Map.Entry::getValue)
            .orElse(new HashMap<>());

        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = holder.getSkills().values();

        final Set<Map.Entry<SkillType, List<BlockChain>>> skillChains = eventChains.entrySet()
            .stream()
            .filter(kv -> skills
                .stream()
                .anyMatch(v -> v.getSkillType() == kv.getKey()))
            .collect(Collectors.toSet());

        if (skillChains.isEmpty()) {
            return;
        }

        final Collection<BuiltinResult> results = this.processBlockSnapshotFor(event.getCause(), skills, skillChains, event.getTargetBlock());

        final List<BuiltinResult> cancelledResults = results
            .stream()
            .filter(result -> result.getType() == Result.Type.CANCELLED)
            .collect(Collectors.toList());

        for (final BuiltinResult cancelledResult : cancelledResults) {
            event.setCancelled(true);

            final Chain<?> chain = cancelledResult.getChain().orElse(null);
            if (chain != null && chain.denyLevelRequired != null) {
                final long stamp = System.currentTimeMillis();
                final Long timer = this.denyTimers.computeIfAbsent(chain, (c) -> stamp - 5000);

                final long diff = stamp - timer;

                if (diff < 3500) {
                    continue;
                }

                this.denyTimers.put(chain, stamp);
                chain.denyLevelRequired.accept(event.getCause(), cancelledResult.getSkill(), chain.level);
            }
        }

        if (event.isCancelled()) {
            return;
        }

        final List<BuiltinResult> successResults = results
            .stream()
            .filter(result -> result.getType() == Result.Type.SUCCESS)
            .collect(Collectors.toList());

        final Map<Skill, Double> totalXPGained = this.calculateXPGainedFor(successResults);
        final Map<Skill, BigDecimal> totalMoneyGained = this.calculateMoneyGainedFor(successResults);

        this.adjustXPAndMoney(event, player, totalXPGained, totalMoneyGained);
    }

    private void handleCraftItem(final SkillService service, final CraftItemEvent.Craft event, final Player player) {
        if (player.gameMode().get() == GameModes.CREATIVE) {
            return;
        }

        final SkillHolderContainer worldContainer = service.getContainer(player.getWorld().getUniqueId()).orElse(null);
        if (worldContainer == null) {
            return;
        }

        final SkillHolderContainer container = service.getParentContainer(worldContainer).orElse(null);
        if (container == null) {
            return;
        }

        final SkillHolder holder = container.getHolder(player.getUniqueId()).orElse(null);

        if (holder == null) {
            return;
        }

        final Map<SkillType, List<ItemChain>> eventChains = this.itemChains.entrySet()
            .stream()
            .filter(kv -> kv.getKey().isAssignableFrom(event.getClass()))
            .findAny()
            .map(Map.Entry::getValue)
            .orElse(new HashMap<>());

        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = holder.getSkills().values();

        final Set<Map.Entry<SkillType, List<ItemChain>>> skillChains = eventChains.entrySet()
            .stream()
            .filter(kv -> skills
                .stream()
                .anyMatch(v -> v.getSkillType() == kv.getKey()))
            .collect(Collectors.toSet());

        if (skillChains.isEmpty()) {
            return;
        }

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
            results.addAll(this.handleItemStack(skills, skillChains, stack));
        }

        final List<BuiltinResult> cancelledResults = results
            .stream()
            .filter(result -> result.getType() == Result.Type.CANCELLED)
            .collect(Collectors.toList());

        for (final BuiltinResult cancelledResult : cancelledResults) {
            event.setCancelled(true);

            final Chain<?> chain = cancelledResult.getChain().orElse(null);

            if (chain != null && chain.denyLevelRequired != null) {
                chain.denyLevelRequired.accept(event.getCause(), cancelledResult.getSkill(), chain.level);
            }
        }

        if (event.isCancelled()) {
            return;
        }

        final List<BuiltinResult> successResults = results
            .stream()
            .filter(result -> result.getType() == Result.Type.SUCCESS)
            .collect(Collectors.toList());

        final Map<Skill, Double> totalXPGained = this.calculateXPGainedFor(successResults);
        final Map<Skill, BigDecimal> totalMoneyGained = this.calculateMoneyGainedFor(successResults);

        this.adjustXPAndMoney(event, player, totalXPGained, totalMoneyGained);
    }

    private void handleDropItemEvent(final SkillService service, final DropItemEvent.Destruct event, final Player player) {
        if (player.gameMode().get() == GameModes.CREATIVE) {
            return;
        }

        final List<Item> items = event.getEntities()
            .stream()
            .filter(entity -> entity instanceof Item)
            .map(entity -> (Item) entity)
            .collect(Collectors.toList());

        if (items.isEmpty()) {
            return;
        }

        final SkillHolderContainer worldContainer = service.getContainer(player.getWorld().getUniqueId()).orElse(null);
        if (worldContainer == null) {
            return;
        }

        final SkillHolderContainer container = service.getParentContainer(worldContainer).orElse(null);
        if (container == null) {
            return;
        }

        final SkillHolder holder = container.getHolder(player.getUniqueId()).orElse(null);

        if (holder == null) {
            return;
        }

        final Map<SkillType, List<ItemChain>> eventChains = this.itemChains.entrySet()
            .stream()
            .filter(kv -> kv.getKey().isAssignableFrom(event.getClass()))
            .findAny()
            .map(Map.Entry::getValue)
            .orElse(new HashMap<>());

        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = holder.getSkills().values();

        final Set<Map.Entry<SkillType, List<ItemChain>>> skillChains = eventChains.entrySet()
            .stream()
            .filter(kv -> skills.stream().anyMatch(v -> v.getSkillType() == kv.getKey()))
            .collect(Collectors.toSet());

        if (skillChains.isEmpty()) {
            return;
        }

        final List<BuiltinResult> successResults = new ArrayList<>();

        for (final Item item : items) {
            final Collection<BuiltinResult> itemResults = this.handleItemStack(skills, skillChains, item.item().get().createStack());
            final List<BuiltinResult> cancelledResults = itemResults
                .stream()
                .filter(result -> result.getType() == Result.Type.CANCELLED)
                .collect(Collectors.toList());

            for (final BuiltinResult cancelledResult : cancelledResults) {
                final Chain<?> chain = cancelledResult.getChain().orElse(null);
                if (chain != null && chain.denyLevelRequired != null) {
                    chain.denyLevelRequired.accept(event.getCause(), cancelledResult.getSkill(), chain.level);
                }

                event.filterEntities(entity -> entity == item);
            }

            successResults.addAll(itemResults
                .stream()
                .filter(result -> result.getType() == Result.Type.SUCCESS)
                .collect(Collectors.toList())
            );
        }

        final Map<Skill, Double> totalXPGained = this.calculateXPGainedFor(successResults);
        final Map<Skill, BigDecimal> totalMoneyGained = this.calculateMoneyGainedFor(successResults);

        this.adjustXPAndMoney(event, player, totalXPGained, totalMoneyGained);
    }

    private Collection<BuiltinResult> processBlockSnapshotFor(final Cause cause, final Collection<Skill> skills, final Set<Map.Entry<SkillType,
        List<BlockChain>>> skillChains, final BlockSnapshot snapshot) {

        final List<BuiltinResult> results = new ArrayList<>();

        for (final Skill skill : skills) {

            final BuiltinResult.Builder builder = BuiltinResult.builder().skill(skill);

            final List<BlockChain> chains = skillChains
                .stream()
                .filter(kv -> kv.getKey() == skill.getSkillType())
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new ArrayList<>());

            final BlockChain chain = chains
                .stream()
                .filter(v -> {
                    if (v.matchOnlyType) {
                        return v.inverseQuery ? v.toQuery.stream().noneMatch(s -> s.getType() == snapshot.getState().getType()) :
                            v.toQuery.stream().anyMatch(s -> s.getType() == snapshot.getState().getType());
                    } else {
                        return v.inverseQuery != v.toQuery.contains(snapshot.getState());
                    }
                })
                .findAny()
                .orElse(null);

            if (chain == null) {
                continue;
            }

            builder.type(Result.Type.SUCCESS);

            if (chain != null) {

                builder.chain(chain);

                boolean cont = true;

                // Check level first to set result cancellation
                if (chain.level != null && chain.level > skill.getCurrentLevel()) {
                    builder.type(Result.Type.CANCELLED);
                    cont = false;
                }

                // Check owner second
                if (cont) {
                    final TriFunction<Cause, Skill, BlockSnapshot, Boolean> owner = chain.owner;
                    if (owner != null) {
                        final Boolean ownerCheck = owner.apply(cause, skill, snapshot);

                        if (ownerCheck != null && !ownerCheck) {
                            cont = false;
                        }
                    }
                }

                if (cont) {
                    if (chain.xp != null) {
                        builder.xp(chain.xp);
                    }

                    if (chain.economy != null) {
                        skill.getSkillType().getEconomyFunction()
                            .ifPresent(func -> builder.money(func.getMoneyFor(skill.getCurrentLevel(), chain.economy)));
                    }
                }
            }

            results.add(builder.build());
        }

        return results;
    }

    private Collection<BuiltinResult> handleItemStack(final Collection<Skill> skills, final Set<Map.Entry<SkillType, List<ItemChain>>>
        skillChains, final ItemStack stack) {

        final List<BuiltinResult> results = new ArrayList<>();

        for (final Skill skill : skills) {

            final BuiltinResult.Builder builder = BuiltinResult.builder().skill(skill);

            final List<ItemChain> chains = skillChains
                .stream()
                .filter(kv -> kv.getKey() == skill.getSkillType())
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new ArrayList<>());

            final ItemChain chain = chains
                .stream()
                .filter(v -> {
                    if (v.matchOnlyType) {
                        return v.inverseQuery ?
                            v.toQuery.stream().noneMatch(s -> ItemStackComparators.TYPE.compare(s, stack) == 0) :
                            v.toQuery.stream().anyMatch(s -> ItemStackComparators.TYPE.compare(s, stack) == 0);
                    } else {
                        return v.inverseQuery ? v.toQuery.stream().noneMatch(s -> ItemStackComparators.IGNORE_SIZE.compare(s, stack) == 0) :
                            v.toQuery.stream().anyMatch(s -> ItemStackComparators.IGNORE_SIZE.compare(s, stack) == 0);
                    }
                })
                .findAny()
                .orElse(null);

            builder.type(Result.Type.SUCCESS);

            if (chain != null) {

                builder.chain(chain);
                if (chain.level != null && chain.level > skill.getCurrentLevel()) {
                    builder.type(Result.Type.CANCELLED);
                } else {
                    if (chain.economy != null) {
                        skill.getSkillType().getEconomyFunction()
                            .ifPresent(func -> builder.money(func.getMoneyFor(skill.getCurrentLevel(), chain.economy)));
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

        this.blockChains.computeIfAbsent(clazz, v -> new HashMap<>()).computeIfAbsent(type, v -> new ArrayList<>()).add(chain);
        return this;
    }

    public BuiltinEventListener addItemChain(final Class<? extends Event> clazz, final SkillType type, final ItemChain chain) {
        checkNotNull(type);
        checkNotNull(chain);

        this.itemChains.computeIfAbsent(clazz, v -> new HashMap<>()).computeIfAbsent(type, v -> new ArrayList<>()).add(chain);
        return this;
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

    private void adjustXPAndMoney(final Event event, final Player player, final Map<Skill, Double> totalXPGained,
        final Map<Skill, BigDecimal> totalMoneyGained) {

        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(event);

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
}
