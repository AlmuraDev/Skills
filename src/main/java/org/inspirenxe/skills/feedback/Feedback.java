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
package org.inspirenxe.skills.feedback;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.logging.log4j.Logger;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.Skills;
import org.inspirenxe.skills.api.event.ChangeExperienceEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.projectile.explosive.FireworkRocket;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * What players see and hear when their experience changes: the XP gained in the action bar, level ups in chat (also broadcast and
 * relayed to Discord), deny messages and level up fireworks.
 *
 * <p>Permissions (per skill, by its lower case name):</p>
 * <ul>
 *     <li>{@code skills.notification.xp.<skill>}: XP gained in the action bar</li>
 *     <li>{@code skills.notification.level.<skill>}: own level ups in chat</li>
 *     <li>{@code skills.notification.level.<skill>.other}: other players' level ups in chat</li>
 *     <li>{@code skills.notification.deny.<action>.<skill>}: the message when a level gate denies an action</li>
 *     <li>{@code skills.effect.firework.<skill>}: a firework on level up (not while sneaking)</li>
 * </ul>
 */
public final class Feedback {

    /**
     * Pushed into the cause by commands, so command changes are not broadcast, relayed or celebrated with fireworks.
     */
    public enum CommandCause {
        INSTANCE
    }

    private static final int MAX_LEVEL_FIREWORKS = 4;

    private final Logger logger;
    private final DiscordRelay discord;
    private volatile Map<SkillType, FireworkEffect> fireworks = Map.of();
    // Rockets we launched; their blast must not hurt anyone
    private final Set<UUID> rockets = ConcurrentHashMap.newKeySet();

    public Feedback(final Logger logger) {
        this.logger = logger;
        this.discord = new DiscordRelay(logger);
    }

    public void setFireworks(final Map<SkillType, FireworkEffect> fireworks) {
        this.fireworks = Map.copyOf(fireworks);
    }

    private static String permissionName(final SkillType type) {
        return type.name().toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    @Listener(order = Order.LATE)
    public void onChangeExperience(final ChangeExperienceEvent.Post event, @First final ServerPlayer player) {
        final Cause cause = event.cause();
        final Skill skill = event.skill();
        final SkillType type = skill.type();
        final String name = Feedback.permissionName(type);
        final double xp = event.experienceDifference();
        if (xp != 0 && player.hasPermission(Skills.ID + ".notification.xp." + name)) {
            player.sendActionBar(Component.text()
                    .append(Component.text((xp >= 0 ? "+ " : "- ") + Skills.service().xpFormat().format(Math.abs(xp)) + "xp "))
                    .append(type.formattedName())
                    .build());
        }
        if (!(event instanceof ChangeExperienceEvent.Post.Level levelEvent) || levelEvent.level() <= levelEvent.originalLevel()) {
            return;
        }
        final int level = levelEvent.level();
        if (player.hasPermission(Skills.ID + ".notification.level." + name)) {
            player.sendMessage(Component.text()
                    .append(Component.text("Congratulations, you just advanced a new "))
                    .append(type.formattedName())
                    .append(Component.text(" level! You are now level " + level + "."))
                    .build());
        }
        if (cause.contains(CommandCause.INSTANCE)) {
            return;
        }
        final String plainSkill = PlainTextComponentSerializer.plainText().serialize(type.formattedName());
        this.discord.relay(":trophy:", player.name() + " has advanced to " + plainSkill + " level " + level + ".");
        final Component broadcast = Component.text()
                .append(Component.text(player.name() + " has advanced to "))
                .append(type.formattedName())
                .append(Component.text(" level " + level + "."))
                .build();
        for (final ServerPlayer other : Sponge.server().onlinePlayers()) {
            if (other != player && other.hasPermission(Skills.ID + ".notification.level." + name + ".other")) {
                other.sendMessage(broadcast);
            }
        }
        this.launchFireworks(player, type, level);
    }

    private void launchFireworks(final ServerPlayer player, final SkillType type, final int level) {
        final FireworkEffect effect = this.fireworks.get(type);
        if (effect == null || player.get(Keys.IS_SNEAKING).orElse(false)
                || !player.hasPermission(Skills.ID + ".effect.firework." + Feedback.permissionName(type))) {
            return;
        }
        final int count = level >= type.maxLevel() ? Feedback.MAX_LEVEL_FIREWORKS : 1;
        final ServerWorld world = player.world();
        for (int i = 0; i < count; i++) {
            try {
                final FireworkRocket rocket = world.createEntity(EntityTypes.FIREWORK_ROCKET.get(), player.position().add(0, 2, 0));
                rocket.offer(Keys.FIREWORK_EFFECTS, List.of(effect));
                this.rockets.add(rocket.uniqueId());
                if (world.spawnEntity(rocket)) {
                    rocket.detonate();
                } else {
                    this.rockets.remove(rocket.uniqueId());
                }
            } catch (final RuntimeException e) {
                this.logger.warn("Failed to launch the level up firework of {} for '{}'.", type.key(), player.name(), e);
                return;
            }
        }
    }

    @Listener(order = Order.EARLY)
    public void onDamageEntity(final DamageEntityEvent event, @First final DamageSource source) {
        if (this.rockets.isEmpty()) {
            return;
        }
        final UUID rocket = source.source().map(entity -> entity.uniqueId()).orElse(null);
        if (rocket != null && this.rockets.contains(rocket)) {
            event.setCancelled(true);
        }
    }

    /**
     * Forgets launched rockets; they detonate on launch, so this only keeps the set small.
     */
    public void clearRockets() {
        this.rockets.clear();
    }

    // Player -> deny message key -> last time it was sent
    private final Map<UUID, Map<Object, Long>> denyTimers = new HashMap<>();

    /**
     * Tells the player which level they need, at most once per {@code throttleMillis} for the same rule.
     */
    public void deny(final ServerPlayer player, final Object rule, final SkillType type, final String action, final int level,
            final long throttleMillis) {
        if (action == null || action.isBlank()) {
            return;
        }
        final long now = System.currentTimeMillis();
        final Map<Object, Long> timers = this.denyTimers.computeIfAbsent(player.uniqueId(), k -> new HashMap<>());
        final Long last = timers.get(rule);
        if (last != null && now - last < throttleMillis) {
            return;
        }
        timers.put(rule, now);
        if (player.hasPermission(Skills.ID + ".notification.deny." + action + "." + Feedback.permissionName(type))) {
            player.sendMessage(Component.text()
                    .append(Component.text("You require "))
                    .append(type.formattedName())
                    .append(Component.text(" level " + level + " to " + action + " this.", NamedTextColor.WHITE))
                    .build());
        }
    }

    public void forget(final UUID player) {
        this.denyTimers.remove(player);
    }

    public void clearDenyTimers() {
        this.denyTimers.clear();
    }
}
