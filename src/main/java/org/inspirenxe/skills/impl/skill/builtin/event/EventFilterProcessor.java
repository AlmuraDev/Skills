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
package org.inspirenxe.skills.impl.skill.builtin.event;

import com.almuradev.toolbox.inject.event.Witness;
import com.almuradev.toolbox.inject.event.WitnessScope;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.event.ChangeExperienceEvent;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.SkillType;
import org.inspirenxe.skills.api.skill.builtin.BasicSkillType;
import org.inspirenxe.skills.api.skill.builtin.EventAction;
import org.inspirenxe.skills.api.skill.builtin.EventProcessor;
import org.inspirenxe.skills.api.skill.holder.SkillHolder;
import org.inspirenxe.skills.api.skill.holder.SkillHolderContainer;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.skill.builtin.registry.module.EventProcessorRegistryModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@WitnessScope.Sponge
public final class EventFilterProcessor implements Witness {

    private final ServiceManager serviceManager;
    private final EventProcessorRegistryModule processorModule;

    @Inject
    public EventFilterProcessor(final ServiceManager serviceManager, final EventProcessorRegistryModule processorModule) {
        this.serviceManager = serviceManager;
        this.processorModule = processorModule;
    }

    @Listener(order = Order.EARLY)
    public void onEvent(final Event event) {

        final SkillService service = this.serviceManager.provide(SkillService.class).orElse(null);
        if (service == null) {
            return;
        }

        final User user = event.getCause().first(User.class).orElse(null);
        if (user == null) {
            return;
        }

        final UUID containerId = user.getWorldUniqueId().orElse(null);
        if (containerId == null) {
            return;
        }

        final SkillHolderContainer container = service.getContainer(containerId).orElse(null);
        if (container == null) {
            return;
        }

        final SkillHolderContainer actualContainer = service.getParentContainer(container).orElse(null);
        if (actualContainer == null) {
            return;
        }

        final SkillHolder holder = actualContainer.getHolder(user.getUniqueId()).orElse(null);
        if (holder == null) {
            return;
        }

        for (final Map.Entry<SkillType, Skill> skillEntry : holder.getSkills().entrySet()) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(skillEntry.getValue());

                for (final EventProcessor processor : this.processorModule.getAll()) {
                    if (processor.shouldProcess(event)) {
                        processor.process(event, service, skillEntry.getValue());
                    }
                }
            }
        }
    }

    @Listener(order = Order.LATE)
    public void onChangeExperience(final ChangeExperienceEvent.Post event) {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        if (event instanceof ChangeExperienceEvent.Post.Level) {
            this.levelChange(event.getCause(), event.getSkill(), ((ChangeExperienceEvent.Post.Level) event).getLevel());
        } else {
            this.xpChange(service, event.getCause(), event.getSkill(), event.getExperienceDifference());
        }
    }

    @Listener(order = Order.PRE)
    public void onDamageEntity(final DamageEntityEvent event, @Root final EntityDamageSource source, @First final Skill skill) {
        if (source.getSource() instanceof Firework) {
            event.setCancelled(true);
        }
    }

    private void xpChange(final SkillService service, final Cause cause, final Skill skill, final double amount) {
        if (!(skill.getSkillType() instanceof BasicSkillType)) {
            return;
        }
        final BasicSkillType skillType = (BasicSkillType) skill.getSkillType();

        final Player player = cause.first(Player.class).orElse(null);
        if (player != null) {
            if (player.hasPermission(SkillsImpl.ID + ".notification.xp." + skill.getSkillType().getName().toLowerCase(Sponge.getServer()
                .getConsole().getLocale()))) {

                final char mod = amount >= 0 ? '+' : '-';

                player.sendMessage(ChatTypes.ACTION_BAR, Text.of(mod, " ", service.getXPFormat().format(Math.abs(amount)), "xp ",
                    skillType.getFormattedName()));
            }
        }

        skillType.onXPChanged(cause, skill, amount);
    }

    private void levelChange(final Cause cause, final Skill skill, final int newLevel) {
        if (!(skill.getSkillType() instanceof BasicSkillType)) {
            return;
        }
        final BasicSkillType skillType = (BasicSkillType) skill.getSkillType();

        final Player player = cause.first(Player.class).orElse(null);
        if (player != null) {
            if (player.hasPermission(SkillsImpl.ID + ".notification.level." + skill.getSkillType().getName().toLowerCase(Sponge.getServer()
                .getConsole().getLocale()))) {
                player.sendMessage(Text.of("Congratulations, you just advanced a new ", skillType.getFormattedName(), " level! You are now"
                    + " level ", newLevel, "."));
            }

            if (!cause.containsType(CommandCallable.class)) {
                Sponge.getServer().getOnlinePlayers().forEach(p -> {
                    if (p != player && p.hasPermission(SkillsImpl.ID + ".notification.level." + skill.getSkillType().getName().toLowerCase(
                        Sponge.getServer().getConsole().getLocale()) + ".other")) {
                        p.sendMessage(ChatTypes.CHAT,
                            Text.of(player.getName(), " has advanced to ", skillType.getFormattedName(), " level ", newLevel, "."));
                    }
                });
            }

            final Boolean sneaking = player.get(Keys.IS_SNEAKING).orElse(false);
            if (!sneaking) {
                if (player.hasPermission(SkillsImpl.ID + ".effect.firework." + skill.getSkillType().getName()
                    .toLowerCase(Sponge.getServer().getConsole().getLocale()))) {

                    skillType.getFireworkEffectFor(newLevel).ifPresent(fireworkEffect -> {
                        for (int i = 0; i < (newLevel == skillType.getMaxLevel() ? 3 : 1); i++) {
                            fireworkEffect.play(player.getLocation());
                        }
                    });
                }
            }
        }

        skillType.onLevelChanged(cause, skill, newLevel);
    }
}
