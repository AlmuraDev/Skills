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
package org.inspirenxe.skills.impl.content.type.skill.builtin.skill;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.effect.firework.FireworkEffectType;
import org.inspirenxe.skills.impl.SkillsConstants;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.content.type.effect.firework.ContentFireworkEffectTypeBuilderImpl;
import org.inspirenxe.skills.impl.content.type.skill.builtin.feedback.EventEffect;
import org.inspirenxe.skills.impl.content.type.skill.builtin.feedback.EventMessage;
import org.inspirenxe.skills.impl.effect.SkillsEffectType;
import org.inspirenxe.skills.impl.effect.firework.SkillsFireworkEffectType;
import org.inspirenxe.skills.impl.util.function.TriConsumer;
import org.inspirenxe.skills.impl.util.function.TriFunction;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class CommonRegistrar {

    @Inject
    private static GameRegistry registry;

    public static EventMessage XP_TO_ACTION_BAR = new EventMessage().chatType(ChatTypes.ACTION_BAR).xpGained(
        (player, skill, xp) -> {
            final char mod = xp >= 0 ? '+' : '-';
            if (player.hasPermission(SkillsImpl.ID + ".notification.xp." + skill.getSkillType().getName().toLowerCase(Sponge.getServer().getConsole().getLocale()))) {
                return Text.of(mod, " ", SkillsConstants.XP_PRINTOUT.format(Math.abs(xp)), "xp ", skill.getSkillType().getFormattedName());
            }
            return null;
        }
    );

    public static EventMessage LEVEL_UP_TO_CHAT = new EventMessage().chatType(ChatTypes.CHAT).levelGained(
        (player, skill, integer) -> {
            if (player.hasPermission(SkillsImpl.ID + ".notification.level." + skill.getSkillType().getName().toLowerCase(Sponge.getServer().getConsole().getLocale()))) {
                return Text.of("Congratulations, you just advanced a new ", skill.getSkillType().getFormattedName(), " level! You are now "
                    + "level ", integer, ".");
            }

            return null;
        }
    );

    @SuppressWarnings("unchecked")
    public static TriConsumer<Player, Skill, Integer> createDenyAction(final String action) {
        return (player, skill, value) -> {
            if (player.hasPermission(SkillsImpl.ID + ".notification.deny." + action + "." + skill.getSkillType().getName().toLowerCase(Sponge.getServer().getConsole().getLocale()))) {
                    player.sendMessage(Text.of("You require ", skill.getSkillType().getFormattedName(), " level ", value, " to "
                        + action + " this."));
            }
        };
    }

    public static TriFunction<Player, Skill, BlockSnapshot, Boolean> CREATOR_ONLY = (p, skill, snapshot) -> {
        final UUID player = p.getUniqueId();
        final UUID creator = snapshot.getCreator().orElse(null);
        if (creator == null) {
            return false;
        }

        return player.equals(creator);
    };

    public static TriFunction<Player, Skill, BlockSnapshot, Boolean> CREATOR_NONE = (p, skill, snapshot) -> {
        final UUID creator = snapshot.getCreator().orElse(null);
        return creator == null;
    };

    public static TriFunction<Player, Skill, BlockSnapshot, Boolean> CREATOR_ANY = (p, skill, snapshot) -> {
        final UUID creator = snapshot.getCreator().orElse(null);
        return creator != null;
    };

    public static TriFunction<Player, Skill, BlockSnapshot, Boolean> CREATOR_OR_NONE = (p, skill, snapshot) -> {
        final UUID player = p.getUniqueId();
        final UUID creator = snapshot.getCreator().orElse(null);
        if (creator == null) {
            return true;
        }

        return player.equals(creator);
    };

    public static TriFunction<Player, Skill, BlockSnapshot, Boolean> CREATOR_OR_ANY = (p, skill, snapshot) -> true;

    public static EventEffect createFireworkEffect(final String effectId) {
        checkNotNull(effectId);

        final SkillsFireworkEffectType fireworkOnLevel = (SkillsFireworkEffectType) registry.getType(
            FireworkEffectType.class, effectId).orElse(null);

        final EventEffect effect = new EventEffect();

        if (fireworkOnLevel == null) {
            return effect;
        }

        return effect.levelGained(
            new TriFunction<Player, Skill, Integer, List<SkillsEffectType>>() {
                private final List<SkillsEffectType> level = new ArrayList<>();
                private final List<SkillsEffectType> levelMax = new ArrayList<>();

                {
                    this.level.add(fireworkOnLevel);
                    this.levelMax.add(fireworkOnLevel);

                    for (int i = 0; i < 3; i++) {
                        final ContentFireworkEffectTypeBuilderImpl builder = new ContentFireworkEffectTypeBuilderImpl();
                        this.levelMax.add(builder.from(fireworkOnLevel).build());
                    }
                }

                @Override
                public List<SkillsEffectType> apply(final Player player, final Skill skill, final Integer integer) {
                    if (!player.hasPermission(SkillsImpl.ID + ".effect.firework." + skill.getSkillType().getName().toLowerCase(Sponge.getServer().getConsole().getLocale()))) {
                        return Collections.emptyList();
                    }

                    if (integer == 99) {
                        return this.levelMax;
                    }

                    return this.level;
                }
            });
    }
}
