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
package org.inspirenxe.skills.impl.content.type.skill.builtin.registar;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.effect.firework.FireworkEffectType;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.content.type.effect.firework.ContentFireworkEffectTypeBuilderImpl;
import org.inspirenxe.skills.impl.content.type.skill.builtin.EventFeedback;
import org.inspirenxe.skills.impl.effect.SkillsEffectType;
import org.inspirenxe.skills.impl.effect.firework.SkillsFireworkEffectType;
import org.inspirenxe.skills.impl.util.function.TriConsumer;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;

import java.util.ArrayList;
import java.util.List;

public final class CommonRegistar {

  @Inject
  private static GameRegistry registry;

  public static EventFeedback XP_TO_ACTION_BAR = new EventFeedback().xpGained(
    (cause, skill, xp) -> {
      final SkillService skillService = Sponge.getServiceManager().provideUnchecked(SkillService.class);

      final Player player = cause.first(Player.class).orElse(null);
      if (player != null) {

        if (player.hasPermission(SkillsImpl.ID + ".notification.xp." + skill.getSkillType().getName().toLowerCase(Sponge.getServer()
          .getConsole().getLocale()))) {

          final char mod = xp >= 0 ? '+' : '-';

          player.sendMessage(ChatTypes.ACTION_BAR, Text.of(mod, " ", skillService.getXPFormat().format(Math.abs(xp)), "xp ", skill
            .getSkillType().getFormattedName()));
        }
      }
    }
  );

  public static EventFeedback LEVEL_UP_TO_CHAT = new EventFeedback().levelGained(
    (cause, skill, level) -> {
      final Player player = cause.first(Player.class).orElse(null);

      if (player == null) {
        return;
      }

      if (player.hasPermission(SkillsImpl.ID + ".notification.level." + skill.getSkillType().getName().toLowerCase(Sponge.getServer()
        .getConsole().getLocale()))) {
        player.sendMessage(Text.of("Congratulations, you just advanced a new ", skill.getSkillType().getFormattedName(), " level! You are now"
          + " level ", level, "."));
      }

      if (!cause.containsType(CommandCallable.class)) {
        Sponge.getServer().getOnlinePlayers().forEach(p -> {
          if (p != player && p.hasPermission(SkillsImpl.ID + ".notification.level." + skill.getSkillType().getName().toLowerCase(
            Sponge.getServer().getConsole().getLocale()) + ".other")) {
            p.sendMessage(ChatTypes.CHAT, Text.of(player.getName(), " has advanced to ", skill.getSkillType().getFormattedName(),
              " level ", level, "."));
          }
        });
      }
    }
  );

  @SuppressWarnings("unchecked")
  public static TriConsumer<Cause, Skill, Integer> createDenyAction(final String action) {
    return (cause, skill, value) -> {
      if (!(cause.root() instanceof Player)) {
        return;
      }

      final Player player = (Player) cause.root();
      if (player.hasPermission(SkillsImpl.ID + ".notification.deny." + action + "." + skill.getSkillType().getName()
        .toLowerCase(Sponge.getServer().getConsole().getLocale()))) {
        player.sendMessage(Text.of("You require ", skill.getSkillType().getFormattedName(), " level ", value, " to "
          + action + " this."));
      }
    };
  }

  public static EventFeedback createFireworkEffect(final String effectId) {
    checkNotNull(effectId);

    final EventFeedback feedback = new EventFeedback();

    final SkillsFireworkEffectType fireworkOnLevel = (SkillsFireworkEffectType) registry.getType(
      FireworkEffectType.class, effectId).orElse(null);

    if (fireworkOnLevel == null) {
      return feedback;
    }

    return feedback.levelGained(new TriConsumer<Cause, Skill, Integer>() {

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
      public void accept(final Cause cause, final Skill skill, final Integer level) {
        final Player player = cause.first(Player.class).orElse(null);

        if (player == null) {
          return;
        }

        if (cause.containsType(CommandCallable.class)) {
          return;
        }

        final Boolean sneaking = player.get(Keys.IS_SNEAKING).orElse(false);
        if (sneaking) {
          return;
        }

        if (!player.hasPermission(
          SkillsImpl.ID + ".effect.firework." + skill.getSkillType().getName().toLowerCase(Sponge.getServer().getConsole().getLocale()))) {
          return;
        }

        final List<SkillsEffectType> effects;

        if (skill.getSkillType().getMaxLevel() == level) {
          effects = this.levelMax;
        } else {
          effects = this.level;
        }

        effects.forEach(effect -> effect.play(player.getLocation()));
      }
    });
  }
}
