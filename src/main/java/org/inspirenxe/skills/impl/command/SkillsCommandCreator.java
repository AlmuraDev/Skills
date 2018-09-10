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

import static org.spongepowered.api.command.args.GenericArguments.catalogedElement;
import static org.spongepowered.api.command.args.GenericArguments.doubleNum;
import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.playerOrSource;
import static org.spongepowered.api.command.args.GenericArguments.seq;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.command.args.GenericArguments.userOrSource;
import static org.spongepowered.api.command.args.GenericArguments.world;

import com.google.inject.Inject;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillManager;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.generated.Tables;
import org.inspirenxe.skills.generated.tables.records.SkillsExperienceRecord;
import org.inspirenxe.skills.impl.SkillHolderImpl;
import org.inspirenxe.skills.impl.SkillsConstants;
import org.inspirenxe.skills.impl.database.DatabaseManager;
import org.inspirenxe.skills.impl.database.Queries;
import org.jooq.DSLContext;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.storage.WorldProperties;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Provider;

public final class SkillsCommandCreator implements Provider<CommandSpec> {

  private final PluginContainer container;
  private final Scheduler scheduler;
  private final GameRegistry registry;
  private final ServiceManager serviceManager;
  private final DatabaseManager databaseManager;
  private final SkillManager skillManager;

  @Inject
  public SkillsCommandCreator(final PluginContainer container, final Scheduler scheduler, final GameRegistry registry,
    final ServiceManager serviceManager, final DatabaseManager databaseManager, final SkillManager skillManager) {
    this.container = container;
    this.scheduler = scheduler;
    this.registry = registry;
    this.serviceManager = serviceManager;
    this.databaseManager = databaseManager;
    this.skillManager = skillManager;
  }

  @Override
  public CommandSpec get() {
    return CommandSpec
      .builder()
      .arguments(userOrSource(Text.of("user")))
      .permission(this.container.getId() + ".command.info")
      .description(Text.of("Displays user skills or list of commands if no user provided in console"))
      .executor((source, args) -> {
        final User user = args.<Player>getOne("user").orElse(null);
        if (user == null) {
          // TODO
          return CommandResult.empty();
        }

        if (user != source && !source.hasPermission(this.container.getId() + ".command.info.other")) {
          source.sendMessage(Text.of(TextColors.RED, "You do not have permission to view another players skills."));
          return CommandResult.empty();
        }

        final UUID container = Sponge.getServer().getDefaultWorld().get().getUniqueId();
        final UUID holder = user.getUniqueId();

        final SkillHolder skillHolder = this.skillManager.getHolder(container, holder).orElse(null);

        if (skillHolder != null) {

          final List<Map.Entry<SkillType, Double>> sorted = skillHolder.getSkills().entrySet()
              .stream()
              .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getCurrentExperience()))
              .entrySet()
              .stream()
              .sorted((o1, o2) -> o1.getKey().getName().compareToIgnoreCase(o2.getKey().getName()))
              .collect(Collectors.toList());

          this.sendSkillsPrintout(source, user, sorted);

        } else {
          final Collection<SkillType> skillTypes = this.registry.getAllOf(SkillType.class);

          this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
              final Map<SkillType, Double> skills = new HashMap<>();

              try (final DSLContext context = this.databaseManager.createContext(true)) {

                for (final SkillType skillType : skillTypes) {
                  final SkillsExperienceRecord record =
                    Queries.createFetchExperienceQuery(container, holder, skillType.getId()).build(context).fetchOne();

                  if (record == null) {
                    skills.put(skillType, 0.0);
                  } else {
                    skills.put(skillType, record.getValue(Tables.SKILLS_EXPERIENCE.EXPERIENCE).doubleValue());
                  }
                }

                final List<Map.Entry<SkillType, Double>> sorted = skills.entrySet()
                  .stream()
                  .sorted((o1, o2) -> o1.getKey().getName().compareToIgnoreCase(o2.getKey().getName()))
                  .collect(Collectors.toList());

                this.scheduler
                  .createTaskBuilder()
                  .execute(() -> {
                    this.sendSkillsPrintout(source, user, sorted);
                  })
                  .submit(this.container);
              } catch (SQLException e) {
                e.printStackTrace();
              }
            }).submit(this.container);
        }

        return CommandResult.success();
      })
      .child(this.createExperienceCommand(), "experience", "xp")
      .build();
  }

  private void sendSkillsPrintout(final CommandSource source, final User user, final List<Map.Entry<SkillType, Double>> sorted) {
    final Collection<Text> skillPrintouts = new LinkedList<>();

    int totalLevel = 0;
    int maxTotalLevel = 0;

    for (Map.Entry<SkillType, Double> entry : sorted) {
      final SkillType skillType = entry.getKey();
      final Double currentExperience = entry.getValue();

      final int currentLevel = skillType.getLevelFunction().getLevelFor(currentExperience);
      final int maxLevel = skillType.getMaxLevel();

      final double maxLevelExperience = skillType.getLevelFunction().getXPFor(maxLevel);
      double toNextLevelExperience = 0;

      if (currentLevel != maxLevel) {
        toNextLevelExperience = skillType.getLevelFunction().getXPFor(currentLevel + 1) - currentExperience;
      }

      skillPrintouts.add(Text.of(skillType.getFormattedName(), " :: Lv. ", currentLevel, " / ", maxLevel));

      skillPrintouts.add(Text.of("  Current XP: ", SkillsConstants.XP_PRINTOUT.format(currentExperience), " / ",
        SkillsConstants.XP_PRINTOUT.format(maxLevelExperience)));

      skillPrintouts.add(Text.of("  Next Level: ", SkillsConstants.XP_PRINTOUT.format(toNextLevelExperience)));

      totalLevel += currentLevel;
      maxTotalLevel += skillType.getMaxLevel();
    }

    final PaginationService pagination = this.serviceManager.provide(PaginationService.class).orElse(null);
    if (pagination != null) {
      pagination
        .builder()
        .title(Text.of(TextColors.RED, source == user ? "My" : user.getName() + " Skills"))
        .contents(skillPrintouts)
        .footer(Text.of(TextColors.YELLOW, "Total Level: ", TextColors.RESET, totalLevel, " / ", maxTotalLevel))
        .build()
        .sendTo(source);
    }
  }

  private CommandSpec createExperienceCommand() {
    return CommandSpec.builder().arguments(
      seq(playerOrSource(Text.of("player")), optional(world(Text.of("world"))), catalogedElement(Text.of("skill"), SkillType.class),
        string(Text.of("mode")), doubleNum(Text.of("xp")))).permission(this.container.getId() + ".command.xp")
      .description(Text.of("Allows adjustment of experience of a skill")).executor((source, args) -> {
        final Player player = args.<Player>getOne("player").orElse(null);
        if (player == null) {
          return CommandResult.empty();
        }

        final WorldProperties world = args.<WorldProperties>getOne("world").orElse(Sponge.getServer().getDefaultWorld().orElse(null));
        if (world == null) {
          return CommandResult.empty();
        }

        final SkillHolder skillHolder = this.skillManager.getHolder(world.getUniqueId(), player.getUniqueId()).orElse(null);
        if (skillHolder == null) {
          final Text message =
            source instanceof ConsoleSource ? Text.of("Player ", player.getName(), " has no skills for World ", world.getWorldName()) :
              Text.of("You do not have any skills for World ", world.getWorldName());

          source.sendMessage(message);
          return CommandResult.empty();
        }

        final SkillType skillType = args.<SkillType>getOne("skill").orElse(null);
        if (skillType == null) {
          return CommandResult.empty();
        }

        final Skill skill = skillHolder.getSkill(skillType).orElse(null);
        if (skill == null) {
          final Text message = source instanceof ConsoleSource ?
            Text.of("Player ", player.getName(), " has no ", skillType.getName(), " skill" + " for World ", world.getWorldName()) :
            Text.of("You do not have the ", skillType.getName(), " skill for World ", world.getWorldName());

          source.sendMessage(message);
          return CommandResult.empty();
        }

        final Double xp = args.<Double>getOne("xp").orElse(null);

        if (xp == null) {
          return CommandResult.empty();
        }

        final String mode = args.<String>getOne(Text.of("mode")).orElse(null);
        if (mode == null) {
          return CommandResult.empty();
        }

        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
          frame.pushCause(player);
          frame.pushCause(new FakeEvent());

          switch (mode.toUpperCase(Sponge.getServer().getConsole().getLocale())) {
            case "ADD":
              skill.addExperience(xp);
              break;
            case "REMOVE":
              skill.addExperience(-xp);
              break;
            case "SET":
              skill.setExperience(xp);
              break;
            default:
              return CommandResult.empty();
          }

          this.skillManager.markDirty(skillHolder);
        }

        return CommandResult.success();
      }).build();
  }

  private static class FakeEvent implements Event {

    @Override
    public Cause getCause() {
      throw new UnsupportedOperationException("Do not call me");
    }
  }
}
