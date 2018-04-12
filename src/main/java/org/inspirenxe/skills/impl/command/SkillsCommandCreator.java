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
package org.inspirenxe.skills.impl.command;

import com.google.inject.Inject;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillManager;
import org.inspirenxe.skills.api.SkillType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.inject.Provider;

public final class SkillsCommandCreator implements Provider<CommandSpec> {

  private static final DecimalFormat prettyExp = new DecimalFormat("###,###.##");
  @Inject
  private PluginContainer container;
  @Inject
  private SkillManager manager;

  @Override
  public CommandSpec get() {
    return CommandSpec.builder()
        .permission(this.container.getId() + ".command.info")
        .description(Text.of("Displays user skills when logged in or commands in console"))
        .executor((source, args) -> {

          if (source instanceof Player) {
            final Player player = (Player) source;

            final SkillHolder holder = this.manager.getHolder(player.getWorld().getUniqueId(), player.getUniqueId()).orElse(null);

            if (holder == null) {
              return CommandResult.success();
            }

            final Collection<Text> skillPrintouts = new LinkedList<>();

            int totalLevel = 0;

            for (Map.Entry<SkillType, Skill> skillEntry : holder.getSkills().entrySet()) {
              final SkillType skillType = skillEntry.getKey();
              final Skill skill = skillEntry.getValue();

              final int currentLevel = skill.getCurrentLevel();
              final int maxLevel = skillType.getMaxLevel();

              final double currentExperience = skill.getCurrentExperience();
              final double maxLevelExperience = skillType.getLevelFunction().getXPFor(maxLevel);
              double toNextLevelExperience = 0;

              if (currentLevel != maxLevel) {
                toNextLevelExperience = skillType.getLevelFunction().getXPFor(currentLevel + 1) - currentExperience;
              }

              skillPrintouts.add(Text.of(TextColors.AQUA, skillType.getName(), TextColors.RESET, " :: Lv. ", currentLevel, "/",
                  maxLevel));

              skillPrintouts.add(Text.of("  Current XP: ", prettyExp.format(currentExperience), "/", prettyExp.format(maxLevelExperience)));

              skillPrintouts.add(Text.of("  Next Level: ", prettyExp.format(toNextLevelExperience)));

              totalLevel += currentLevel;
            }
            final PaginationService pagination = Sponge.getServiceManager().provide(PaginationService.class).orElse(null);
            if (pagination != null) {
              pagination.builder()
                .title(Text.of(TextColors.RED, "My Skills"))
                .contents(skillPrintouts)
                .footer(Text.of(TextColors.YELLOW, "Total Level: ", TextColors.RESET, totalLevel))
                .build()
                .sendTo(player);
            }
          }

          return CommandResult.success();
        })
        .build();
  }
}
