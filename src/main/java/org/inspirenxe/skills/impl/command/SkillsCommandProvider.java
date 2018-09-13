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
import static org.spongepowered.api.command.args.GenericArguments.seq;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.command.args.GenericArguments.userOrSource;
import static org.spongepowered.api.command.args.GenericArguments.world;

import com.google.inject.Inject;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillHolderContainer;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.generated.Tables;
import org.inspirenxe.skills.generated.tables.records.SkillsExperienceRecord;
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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.storage.WorldProperties;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Provider;

public final class SkillsCommandProvider implements Provider<CommandSpec> {

  private final PluginContainer container;
  private final Scheduler scheduler;
  private final GameRegistry registry;
  private final ServiceManager serviceManager;
  private final DatabaseManager databaseManager;

  @Inject
  public SkillsCommandProvider(final PluginContainer container, final Scheduler scheduler, final GameRegistry registry,
    final ServiceManager serviceManager, final DatabaseManager databaseManager) {
    this.container = container;
    this.scheduler = scheduler;
    this.registry = registry;
    this.serviceManager = serviceManager;
    this.databaseManager = databaseManager;
  }

  @Override
  public CommandSpec get() {
    return CommandSpec
      .builder()
      .arguments(seq(userOrSource(Text.of("user")), optional(world(Text.of("world")))))
      .permission(this.container.getId() + ".command.info")
      .description(Text.of("Displays user skills or list of commands if no user provided in console."))
      .executor((source, args) -> {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        final User user = args.<Player>getOne("user").orElse(null);
        if (user == null) {
          return CommandResult.empty();
        }

        if (user != source && !source.hasPermission(this.container.getId() + ".command.info.other")) {
          source.sendMessage(Text.of(TextColors.RED, "You do not have permission to view another player's skills."));
          return CommandResult.success();
        }

        final UUID holderId = user.getUniqueId();

        final WorldProperties world =
          args.<WorldProperties>getOne("world").orElse(source instanceof Player ? ((Player) source).getWorld().getProperties() :
            Sponge.getServer().getDefaultWorld().orElse(null));
        if (world == null) {
          return CommandResult.empty();
        }

        SkillHolderContainer container;

        final boolean tempWorldContainer;
        SkillHolderContainer worldContainer = service.getContainer(world.getUniqueId()).orElse(null);

        if (worldContainer == null) {
          worldContainer = service.createContainer(world.getUniqueId(), world.getWorldName());
          tempWorldContainer = true;
        } else {
          tempWorldContainer = false;
        }

        container = service.getParentContainer(worldContainer).orElse(null);

        if (container == null) {
          return CommandResult.success();
        }

        final UUID containerId = container.getUniqueId();

        final SkillHolder holder = container.getHolder(holderId).orElse(null);
        if (holder != null) {
          final List<Map.Entry<SkillType, Double>> sorted = holder.getSkills().entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().getCurrentExperience()))
            .entrySet()
            .stream()
            .sorted((o1, o2) -> o1.getKey().getName().compareToIgnoreCase(o2.getKey().getName()))
            .collect(Collectors.toList());

          this.handleSkillsPrintout(service, source, source == user ? "My" : holder.getName(), container.getName(), sorted);
          return CommandResult.success();
        }

        final Collection<SkillType> skillTypes = this.registry.getAllOf(SkillType.class);

        this.scheduler
          .createTaskBuilder()
          .async()
          .execute(() -> {
            final Map<SkillType, Double> skills = new HashMap<>();

            try (final DSLContext context = this.databaseManager.createContext(true)) {

              for (final SkillType skillType : skillTypes) {
                final SkillsExperienceRecord record =
                  Queries
                    .createFetchExperienceQuery(containerId, holderId, skillType.getId())
                    .build(context)
                    .fetchOne();

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
                  if (source instanceof Player) {
                    if (!((Player) source).isOnline()) {
                      return;
                    }
                  }

                  this.handleSkillsPrintout(service, source, source == user ? "My" : user.getName(), container.getName(), sorted);

                  if (tempWorldContainer) {
                    service.removeContainer(containerId);
                  }
                })
                .submit(this.container);
            } catch (final SQLException e) {
              e.printStackTrace();
            }
          }).submit(this.container);

        return CommandResult.success();
      })
      .child(this.createExperienceCommand(), "experience", "xp")
      .child(this.createResetCommand(), "reset")
      .build();
  }

  private CommandSpec createExperienceCommand() {
    return CommandSpec
      .builder()
      .arguments(seq(userOrSource(Text.of("user")), optional(world(Text.of("world"))), catalogedElement(Text.of("skill"),
        SkillType.class), string(Text.of("mode")), doubleNum(Text.of("xp"))))
      .permission(this.container.getId() + ".command.xp")
      .description(Text.of("Allows adjustment of experience of a skill."))
      .executor((source, args) -> {
        final SkillService skillService = this.serviceManager.provideUnchecked(SkillService.class);

        final User user = args.<Player>getOne("user").orElse(null);
        if (user == null) {
          return CommandResult.empty();
        }

        if (user != source && !source.hasPermission(this.container.getId() + ".command.xp.other")) {
          source.sendMessage(Text.of(TextColors.RED, "You do not have permission to modify the xp of other user's skills."));
          return CommandResult.success();
        }

        final UUID holderId = user.getUniqueId();

        final WorldProperties world = args.<WorldProperties>getOne("world").orElse(Sponge.getServer().getDefaultWorld().orElse(null));
        if (world == null) {
          return CommandResult.empty();
        }

        final SkillHolderContainer worldContainer = skillService.getContainer(world.getUniqueId()).orElse(null);
        if (worldContainer == null) {
          // TODO Message
          return CommandResult.success();
        }

        final SkillHolderContainer container = skillService.getParentContainer(worldContainer).orElse(null);
        if (container == null) {
          // TODO Message
          return CommandResult.success();
        }

        final UUID containerId = container.getUniqueId();

        final SkillType type = args.<SkillType>getOne("skill").orElse(null);
        if (type == null) {
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

        boolean validMode = "ADD".equalsIgnoreCase(mode) || "REMOVE".equalsIgnoreCase(mode) || "SET".equalsIgnoreCase(mode);

        if (!validMode) {
          return CommandResult.empty();
        }

        final SkillHolder holder = container.getHolder(holderId).orElse(null);

        // In-memory holder
        if (holder != null) {
          return this.handleSkillsXP(source, user, container, holder, type, mode, xp);
        } else {
          // Database set
          this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
              try (final DSLContext context = this.databaseManager.createContext(true)) {
                boolean update;
                Double currentXp = 0.0;

                if ("SET".equalsIgnoreCase(mode)) {
                  currentXp = xp;

                  final int result = Queries
                    .createHasExperienceInSkillQuery(containerId, holderId, type.getId())
                    .build(context)
                    .execute();

                  update = result == 1;
                } else {
                  final SkillsExperienceRecord record = Queries
                    .createFetchExperienceQuery(containerId, holderId, type.getId())
                    .build(context)
                    .fetchOne();

                  if (record != null) {
                    final double dbXp = record.getValue(Tables.SKILLS_EXPERIENCE.EXPERIENCE).doubleValue();

                    if ("ADD".equalsIgnoreCase(mode)) {
                      currentXp = dbXp + xp;
                    } else if ("REMOVE".equalsIgnoreCase(mode)) {
                      currentXp = dbXp - xp;
                    }

                    update = true;
                  } else {
                    if ("ADD".equalsIgnoreCase(mode)) {
                      currentXp = xp;
                    }

                    update = false;
                  }
                }

                final boolean finalUpdate = update;
                final Double finalXp = currentXp;

                this.scheduler
                  .createTaskBuilder()
                  .execute(() -> {

                    if (finalXp < 0) {
                      // TODO message
                      return;
                    }

                    final SkillHolder potHolder = container.getHolder(holderId).orElse(null);

                    if (potHolder != null) {
                      // Hmm, they came online! We need to run this through the normal systems..

                      this.handleSkillsXP(source, user, container, potHolder, type, mode, finalXp);
                      return;
                    }

                    boolean success;

                    if (!finalUpdate) {
                      final int execute = Queries
                        .createInsertSkillExperienceQuery(containerId, holderId, type.getId(), finalXp)
                        .build(context)
                        .execute();

                      success = execute != 0;
                    } else {
                      final int execute = Queries
                        .createUpdateSkillExperienceQuery(containerId, holderId, type.getId(), finalXp, Timestamp.from(Instant.now()))
                        .build(context)
                        .execute();

                      success = execute != 0;
                    }

                    if (success) {
                      if (source instanceof Player && !((Player) source).isOnline()) {
                        return;
                      }

                      source.sendMessage(Text.of(user.getName(), " had their xp in ", type.getFormattedName(), " set to ",
                        skillService.getXPFormat().format(finalXp), " for Container ", TextColors.GRAY, container.getName(),
                        TextColors.RESET, "."));
                    }
                  })
                  .submit(this.container);
              } catch (final SQLException e) {
                e.printStackTrace();
              }
            })
            .submit(this.container);
        }

        return CommandResult.success();
      }).build();
  }

  private CommandSpec createResetCommand() {
    return CommandSpec
      .builder()
      .arguments(seq(userOrSource(Text.of("user")), optional(world(Text.of("world")))))
      .permission(this.container.getId() + ".command.reset")
      .description(Text.of("Allows resetting of a user's skills."))
      .executor((source, args) -> {
        final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

        final User user = args.<Player>getOne("user").orElse(null);
        if (user == null) {
          return CommandResult.empty();
        }

        if (user != source && !source.hasPermission(this.container.getId() + ".command.reset.other")) {
          source.sendMessage(Text.of(TextColors.RED, "You do not have permission to reset the xp of other user's skills."));
          return CommandResult.success();
        }

        final UUID holderId = user.getUniqueId();

        final WorldProperties world = args.<WorldProperties>getOne("world").orElse(Sponge.getServer().getDefaultWorld().orElse(null));
        if (world == null) {
          return CommandResult.empty();
        }

        final SkillHolderContainer worldContainer = service.getContainer(world.getUniqueId()).orElse(null);
        if (worldContainer == null) {
          // TODO Message
          return CommandResult.success();
        }

        final SkillHolderContainer container = service.getParentContainer(worldContainer).orElse(null);
        if (container == null) {
          // TODO Message
          return CommandResult.success();
        }

        final UUID containerId = container.getUniqueId();

        final boolean load;

        load = container.removeHolder(holderId).isPresent();

        final Collection<SkillType> skillTypes = this.registry.getAllOf(SkillType.class);

        this.scheduler
          .createTaskBuilder()
          .async()
          .execute(() -> {
            try (final DSLContext context = this.databaseManager.createContext(true)) {
              for (final SkillType skillType : skillTypes) {
                Queries
                  .createUpdateSkillExperienceQuery(containerId, holderId, skillType.getId(), 0.0, Timestamp.from(Instant.now()))
                  .build(context)
                  .execute();
              }

              this.scheduler
                .createTaskBuilder()
                .execute(() -> {
                  if (load) {
                    try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                      // TODO Context for target
                      container.createHolder(holderId, user.getName()).getSkills().values().forEach(Skill::load);
                    }
                  }
                })
                .submit(this.container);
            } catch (final SQLException e) {
              e.printStackTrace();
            }
          })
          .submit(this.container);
        return CommandResult.success();
      })
      .build();
  }

  private void handleSkillsPrintout(final SkillService service, final MessageReceiver receiver, final String holderName, final String containerName,
    final List<Map.Entry<SkillType, Double>> sorted) {

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

      skillPrintouts.add(Text.of("  Current XP: ", service.getXPFormat().format(currentExperience), " / ",
        service.getXPFormat().format(maxLevelExperience)));

      skillPrintouts.add(Text.of("  Next Level: ", service.getXPFormat().format(toNextLevelExperience)));

      totalLevel += currentLevel;
      maxTotalLevel += skillType.getMaxLevel();
    }

    final PaginationService pagination = this.serviceManager.provide(PaginationService.class).orElse(null);
    if (pagination != null) {
      pagination
        .builder()
        .title(Text.of(TextColors.RED, holderName, " Skills ", TextColors.RESET, "(", TextColors.GRAY, containerName, TextColors.RESET, ")"))
        .contents(skillPrintouts)
        .footer(Text.of(TextColors.YELLOW, "Total Level: ", TextColors.RESET, totalLevel, " / ", maxTotalLevel))
        .build()
        .sendTo(receiver);
    }
  }

  private CommandResult handleSkillsXP(final CommandSource source, final User target, final SkillHolderContainer container, final SkillHolder holder,
    final SkillType skillType, final String mode, final double xp) {

    final boolean feedback = source instanceof ConsoleSource || (source instanceof Player && ((Player) source).isOnline());

    final Skill skill = holder.getSkill(skillType).orElse(null);
    if (skill == null) {
      if (feedback) {
        final Text message = source instanceof ConsoleSource ?
          Text.of("Holder ", holder.getName(), " has no ", skillType.getFormattedName(), " skill for Container ", TextColors.GRAY, container.getName(),
            TextColors.RESET, ".") :
          Text.of("You do not have the ", skillType.getFormattedName(), " skill for Container ", TextColors.GRAY, container.getName(),
            TextColors.RESET, ".");

        source.sendMessage(message);
      }
      return CommandResult.empty();
    }

    try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
      // TODO Target needs to go into context
      frame.pushCause(target);

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
      }

      skill.save();
    }

    return CommandResult.success();
  }
}
