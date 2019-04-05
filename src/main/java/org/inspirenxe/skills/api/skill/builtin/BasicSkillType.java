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
package org.inspirenxe.skills.api.skill.builtin;

import static com.google.common.base.Preconditions.checkNotNull;

import net.kyori.filter.Filter;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.effect.firework.FireworkEffectType;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.SkillType;
import org.inspirenxe.skills.api.skill.builtin.filter.applicator.TriggerFilter;
import org.inspirenxe.skills.api.skill.holder.SkillHolderContainer;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;

import java.util.List;
import java.util.Optional;

public abstract class BasicSkillType implements SkillType {

    private final SkillService service;
    private final PluginContainer container;
    private final String id, name;
    private final LevelFunctionType levelFunction;
    private final Text formattedName;
    private final int maxLevel;

    protected BasicSkillType(final PluginContainer container, final String id, final String name, final Text formattedName, final LevelFunctionType levelFunction,
        final int maxLevel) {
        this.service = Sponge.getServiceManager().provideUnchecked(SkillService.class);
        this.container = checkNotNull(container);
        this.id = container.getId() + ":" + id;
        this.name = checkNotNull(name);
        this.formattedName = checkNotNull(formattedName);
        this.levelFunction = checkNotNull(levelFunction);
        this.maxLevel = maxLevel;
    }

    @Override
    public final String getId() {
        return this.id;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final PluginContainer getPlugin() {
        return this.container;
    }

    @Override
    public final int getMaxLevel() {
        return this.maxLevel;
    }

    @Override
    public LevelFunctionType getLevelFunction() {
        return this.levelFunction;
    }

    @Override
    public Text getFormattedName() {
        return this.formattedName;
    }

    public final <F extends Filter> BasicSkillType registerCancelEventFilter(final SkillHolderContainer container,
        final Class<? extends Event> eventClass, final F filter) {
        return this;
    }

    public final <F extends Filter> BasicSkillType registerCancelTransactionFilter(final SkillHolderContainer container,
        final Class<? extends Event> eventClass, final F filter) {
        return this;
    }

    public final BasicSkillType registerTransactionTrigger(final SkillHolderContainer container,
        final Class<? extends Event> eventClass, final TriggerFilter filter) {
        return this;
    }

    public abstract void configure(final List<SkillHolderContainer> containers);

    public void onActionDenied(final Cause cause, final Skill skill, final int levelRequired, final EventAction action) {
    }

    public void onXPChanged(final Cause cause, final Skill skill, final double amount) {
    }

    public void onLevelChanged(final Cause cause, final Skill skill, final int newLevel) {
    }

    public Optional<FireworkEffectType> getFireworkEffectFor(final int level) {
        return Optional.empty();
    }

    private void denyAction(final Cause cause, final Skill skill, final int levelRequired, final EventAction action) {
        final Player player = cause.first(Player.class).orElse(null);
        if (player != null) {

            if (player.hasPermission(SkillsImpl.ID + ".notification.deny." + action + "." + skill.getSkillType().getName()
                .toLowerCase(Sponge.getServer().getConsole().getLocale()))) {
                player.sendMessage(Text.of("You require ", this.formattedName, " level ", levelRequired, " to " + action + " this."));
            }
        }

        this.onActionDenied(cause, skill, levelRequired, action);
    }

    private void xpChange(final Cause cause, final Skill skill, final double amount) {
        final Player player = cause.first(Player.class).orElse(null);
        if (player != null) {
            if (player.hasPermission(SkillsImpl.ID + ".notification.xp." + skill.getSkillType().getName().toLowerCase(Sponge.getServer()
                .getConsole().getLocale()))) {

                final char mod = amount >= 0 ? '+' : '-';

                player.sendMessage(ChatTypes.ACTION_BAR, Text.of(mod, " ", this.service.getXPFormat().format(Math.abs(amount)), "xp ",
                    this.formattedName));
            }
        }

        this.onXPChanged(cause, skill, amount);
    }

    private void levelChange(final Cause cause, final Skill skill, final int newLevel) {
        final Player player = cause.first(Player.class).orElse(null);
        if (player != null) {
            if (player.hasPermission(SkillsImpl.ID + ".notification.level." + skill.getSkillType().getName().toLowerCase(Sponge.getServer()
                .getConsole().getLocale()))) {
                player.sendMessage(Text.of("Congratulations, you just advanced a new ", this.formattedName, " level! You are now"
                    + " level ", newLevel, "."));
            }

            if (!cause.containsType(CommandCallable.class)) {
                Sponge.getServer().getOnlinePlayers().forEach(p -> {
                    if (p != player && p.hasPermission(SkillsImpl.ID + ".notification.level." + skill.getSkillType().getName().toLowerCase(
                        Sponge.getServer().getConsole().getLocale()) + ".other")) {
                        p.sendMessage(ChatTypes.CHAT, Text.of(player.getName(), " has advanced to ", this.formattedName, " level ", newLevel, "."));
                    }
                });
            } else {
                final Boolean sneaking = player.get(Keys.IS_SNEAKING).orElse(false);
                if (!sneaking) {
                    if (player.hasPermission(SkillsImpl.ID + ".effect.firework." + skill.getSkillType().getName()
                        .toLowerCase(Sponge.getServer().getConsole().getLocale()))) {
                        this.getFireworkEffectFor(newLevel).ifPresent(fireworkEffect -> {
                            for (int i = 0; i < (newLevel == maxLevel ? 3 : 1); i++) {
                                fireworkEffect.play(player.getLocation());
                            }
                        });
                    }
                }
            }
        }

        this.onLevelChanged(cause, skill, newLevel);
    }
}
