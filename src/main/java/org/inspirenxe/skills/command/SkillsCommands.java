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
package org.inspirenxe.skills.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.inspirenxe.skills.Tasks;
import org.inspirenxe.skills.api.ExperienceResult;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillHolderContainer;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.Skills;
import org.inspirenxe.skills.api.function.LevelFunction;
import org.inspirenxe.skills.database.Database;
import org.inspirenxe.skills.feedback.Feedback;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * {@code /skills [user] [world]}, {@code /skills xp <user> [world] <skill> <add|remove|set> <xp>}, {@code /skills reset <user> [world]}
 * and {@code /skills reload}.
 */
public final class SkillsCommands {

    private static final String PERMISSION = Skills.ID + ".command.";

    private final Logger logger;
    private final Supplier<Database> database;
    private final Runnable reload;

    private final Parameter.Value<UUID> user = Parameter.user().key("user").optional().build();
    private final Parameter.Value<UUID> requiredUser = Parameter.user().key("user").build();
    private final Parameter.Value<ServerWorld> world = Parameter.world().key("world").optional().build();
    private final Parameter.Value<SkillType> skill = Parameter.choices(SkillType.class,
            id -> Skills.service().skillType(id).orElse(null),
            () -> Skills.isAvailable() ? Skills.service().skillTypes().stream().map(type -> type.key().value()).toList() : List.of())
            .key("skill")
            .build();
    private final Parameter.Value<String> mode = Parameter.choices("add", "remove", "set").key("mode").build();
    private final Parameter.Value<Double> xp = Parameter.doubleNumber().key("xp").build();

    public SkillsCommands(final Logger logger, final Supplier<Database> database, final Runnable reload) {
        this.logger = logger;
        this.database = database;
        this.reload = reload;
    }

    private Database database() {
        final Database database = this.database.get();
        if (database == null) {
            throw new IllegalStateException("The Skills database is not open!");
        }
        return database;
    }

    public Command.Parameterized create() {
        return Command.builder()
                .shortDescription(Component.text("Shows the skills of a player."))
                .permission(SkillsCommands.PERMISSION + "info")
                .addParameter(this.user)
                .addParameter(this.world)
                .executor(this::info)
                .addChild(Command.builder()
                        .shortDescription(Component.text("Adds, removes or sets the experience of a skill."))
                        .permission(SkillsCommands.PERMISSION + "xp")
                        .addParameter(this.requiredUser)
                        .addParameter(this.world)
                        .addParameter(this.skill)
                        .addParameter(this.mode)
                        .addParameter(this.xp)
                        .executor(this::experience)
                        .build(), "xp", "experience")
                .addChild(Command.builder()
                        .shortDescription(Component.text("Resets every skill of a player to 0 experience."))
                        .permission(SkillsCommands.PERMISSION + "reset")
                        .addParameter(this.requiredUser)
                        .addParameter(this.world)
                        .executor(this::reset)
                        .build(), "reset")
                .addChild(Command.builder()
                        .shortDescription(Component.text("Reloads the rules and fireworks of the skill files."))
                        .permission(SkillsCommands.PERMISSION + "reload")
                        .executor(context -> {
                            this.reload.run();
                            context.sendMessage(Component.text("Reloaded the Skills rules.", NamedTextColor.GREEN));
                            return CommandResult.success();
                        })
                        .build(), "reload")
                .build();
    }

    private CommandResult info(final CommandContext context) throws CommandException {
        final Optional<ServerPlayer> self = context.cause().first(ServerPlayer.class);
        final UUID target = context.one(this.user).orElse(self.map(ServerPlayer::uniqueId).orElse(null));
        if (target == null) {
            return CommandResult.error(Component.text("Name a player: /skills <player> [world]"));
        }
        final boolean own = self.map(player -> player.uniqueId().equals(target)).orElse(false);
        if (!own && !context.hasPermission(SkillsCommands.PERMISSION + "info.other")) {
            return CommandResult.error(Component.text("You do not have permission to view another player's skills."));
        }
        final SkillService service = Skills.service();
        final SkillHolderContainer container = this.container(context, service);
        final String name = SkillsCommands.name(target);
        final Audience audience = context.cause().audience();
        final SkillHolder holder = container.holder(target).orElse(null);
        if (holder != null) {
            final Map<SkillType, Double> experience = new LinkedHashMap<>();
            for (final SkillType type : service.skillTypes()) {
                experience.put(type, holder.skill(type).map(Skill::experience).orElse(0.0));
            }
            this.print(audience, own ? "My" : name, container, experience);
            return CommandResult.success();
        }
        final String containerKey = container.key().asString();
        this.database().execute(() -> {
            final Map<String, Double> stored;
            try {
                stored = this.database().fetchAll(containerKey, target);
            } catch (final Exception e) {
                this.logger.error("Failed to read the skills of '{}' for /skills!", name, e);
                Tasks.sync(() -> audience.sendMessage(Component.text("Reading the skills failed, see the server log.", NamedTextColor.RED)));
                return;
            }
            Tasks.sync(() -> {
                final Map<SkillType, Double> experience = new LinkedHashMap<>();
                for (final SkillType type : service.skillTypes()) {
                    experience.put(type, stored.getOrDefault(type.key().asString(), 0.0));
                }
                this.print(audience, own ? "My" : name, container, experience);
            });
        }).exceptionally(e -> {
            audience.sendMessage(Component.text("The Skills database is not available.", NamedTextColor.RED));
            return null;
        });
        return CommandResult.success();
    }

    private CommandResult experience(final CommandContext context) throws CommandException {
        final UUID target = context.requireOne(this.requiredUser);
        final boolean own = context.cause().first(ServerPlayer.class).map(player -> player.uniqueId().equals(target)).orElse(false);
        if (!own && !context.hasPermission(SkillsCommands.PERMISSION + "xp.other")) {
            return CommandResult.error(Component.text("You do not have permission to change the experience of another player's skills."));
        }
        final SkillService service = Skills.service();
        final SkillHolderContainer container = this.container(context, service);
        final SkillType type = context.requireOne(this.skill);
        final String mode = context.requireOne(this.mode);
        final double amount = context.requireOne(this.xp);
        if (amount < 0) {
            return CommandResult.error(Component.text("The experience must be positive."));
        }
        final String name = SkillsCommands.name(target);
        final Audience audience = context.cause().audience();
        final SkillHolder holder = container.holder(target).orElse(null);
        if (holder != null) {
            final Skill skill = holder.skill(type).orElse(null);
            if (skill == null) {
                return CommandResult.error(Component.text(name + " has no " + type.name() + " skill."));
            }
            final ExperienceResult result;
            try (CauseStackManager.StackFrame frame = Sponge.server().causeStackManager().pushCauseFrame()) {
                frame.pushCause(Feedback.CommandCause.INSTANCE);
                Sponge.server().player(target).ifPresent(frame::pushCause);
                result = switch (mode) {
                    case "add" -> skill.addExperience(amount);
                    case "remove" -> skill.addExperience(-amount);
                    default -> skill.setExperience(amount);
                };
            }
            if (result.type() != ExperienceResult.Type.SUCCESS) {
                return CommandResult.error(Component.text("Changing the experience of " + name + " in " + type.name() + " failed"
                        + result.exception().map(e -> ": " + e.getMessage()).orElse(" (" + result.type() + ")") + "."));
            }
            skill.save();
            audience.sendMessage(SkillsCommands.changed(name, type, skill.experience(), container));
            return CommandResult.success();
        }
        // Not online in that container: change the database. Queued on the database thread, so a load for a holder created after
        // this point sees the write.
        final String containerKey = container.key().asString();
        final String skillKey = type.key().asString();
        this.database().execute(() -> {
            try {
                final double current = this.database().fetch(containerKey, target, skillKey).orElse(0);
                final double updated = switch (mode) {
                    case "add" -> current + amount;
                    case "remove" -> Math.max(0, current - amount);
                    default -> amount;
                };
                this.database().store(containerKey, target, skillKey, updated);
                Tasks.sync(() -> audience.sendMessage(SkillsCommands.changed(name, type, updated, container)));
            } catch (final Exception e) {
                this.logger.error("Failed to change the experience of '{}' in skill '{}'!", name, skillKey, e);
                Tasks.sync(() -> audience.sendMessage(Component.text("Changing the experience failed, see the server log.",
                        NamedTextColor.RED)));
            }
        }).exceptionally(e -> {
            audience.sendMessage(Component.text("The Skills database is not available.", NamedTextColor.RED));
            return null;
        });
        return CommandResult.success();
    }

    private CommandResult reset(final CommandContext context) throws CommandException {
        final UUID target = context.requireOne(this.requiredUser);
        final boolean own = context.cause().first(ServerPlayer.class).map(player -> player.uniqueId().equals(target)).orElse(false);
        if (!own && !context.hasPermission(SkillsCommands.PERMISSION + "reset.other")) {
            return CommandResult.error(Component.text("You do not have permission to reset another player's skills."));
        }
        final SkillService service = Skills.service();
        final SkillHolderContainer container = this.container(context, service);
        final String name = SkillsCommands.name(target);
        final Audience audience = context.cause().audience();
        // Dropped without saving: its experience is what is being reset
        final boolean wasLive = container.removeHolder(target).isPresent();
        final String containerKey = container.key().asString();
        final List<String> skillKeys = new ArrayList<>();
        for (final SkillType type : service.skillTypes()) {
            skillKeys.add(type.key().asString());
        }
        // Queued on the database thread, behind any pending save of the removed holder
        this.database().execute(() -> {
            try {
                for (final String skillKey : skillKeys) {
                    this.database().store(containerKey, target, skillKey, 0);
                }
            } catch (final Exception e) {
                this.logger.error("Failed to reset the skills of '{}'!", name, e);
            }
            Tasks.sync(() -> {
                if (wasLive && container.holder(target).isEmpty()) {
                    final Optional<ServerPlayer> player = Sponge.server().player(target);
                    if (player.isPresent()) {
                        container.createHolder(target, player.get().name()).skills().values().forEach(Skill::load);
                    }
                }
                audience.sendMessage(Component.text("Reset the skills of " + name + " in " + container.key().asString() + "."));
            });
        }).exceptionally(e -> {
            audience.sendMessage(Component.text("The Skills database is not available.", NamedTextColor.RED));
            return null;
        });
        return CommandResult.success();
    }

    private SkillHolderContainer container(final CommandContext context, final SkillService service) throws CommandException {
        final ServerWorld world = context.one(this.world)
                .or(() -> context.cause().first(ServerPlayer.class).map(ServerPlayer::world))
                .or(() -> Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT))
                .orElseThrow(() -> new CommandException(Component.text("Name a world.")));
        return service.containerFor(world.key()).orElseThrow(() -> new CommandException(
                Component.text("Skills has no data for the world " + world.key().asString() + " (or the world it shares from isn't loaded).")));
    }

    private void print(final Audience audience, final String holderName, final SkillHolderContainer container,
            final Map<SkillType, Double> experience) {
        final SkillService service = Skills.service();
        final List<Component> lines = new ArrayList<>();
        int totalLevel = 0;
        int maxTotalLevel = 0;
        for (final Map.Entry<SkillType, Double> entry : experience.entrySet()) {
            final SkillType type = entry.getKey();
            final double current = entry.getValue();
            final LevelFunction function = type.levelFunction();
            final int level = Math.max(1, function.levelFor(current));
            final int maxLevel = type.maxLevel();
            double toNext = 0;
            if (level < maxLevel) {
                final double next = function.xpFor(level + 1);
                if (next != LevelFunction.UNKNOWN_EXP) {
                    toNext = Math.max(0, next - current);
                }
            }
            lines.add(Component.text().append(type.formattedName()).append(Component.text(" :: Lv. " + level + " / " + maxLevel)).build());
            lines.add(Component.text("  Current XP: " + service.xpFormat().format(current) + " / "
                    + service.xpFormat().format(function.xpFor(maxLevel))));
            lines.add(Component.text("  Next Level: " + service.xpFormat().format(toNext)));
            totalLevel += level;
            maxTotalLevel += maxLevel;
        }
        Sponge.serviceProvider().paginationService().builder()
                .title(Component.text()
                        .append(Component.text(holderName + " Skills ", NamedTextColor.RED))
                        .append(Component.text("("))
                        .append(Component.text(container.key().asString(), NamedTextColor.GRAY))
                        .append(Component.text(")"))
                        .build())
                .contents(lines)
                .footer(Component.text()
                        .append(Component.text("Total Level: ", NamedTextColor.YELLOW))
                        .append(Component.text(totalLevel + " / " + maxTotalLevel))
                        .build())
                .sendTo(audience);
    }

    private static Component changed(final String name, final SkillType type, final double experience, final SkillHolderContainer container) {
        return Component.text()
                .append(Component.text(name + " now has " + Skills.service().xpFormat().format(experience) + " xp in "))
                .append(type.formattedName())
                .append(Component.text(" for "))
                .append(Component.text(container.key().asString(), NamedTextColor.GRAY))
                .append(Component.text("."))
                .build();
    }

    private static String name(final UUID player) {
        return Sponge.server().player(player).map(ServerPlayer::name)
                .or(() -> Sponge.server().gameProfileManager().cache().findById(player).flatMap(profile -> profile.name()))
                .orElse(player.toString());
    }
}
