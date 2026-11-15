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
package org.inspirenxe.skills;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.Skills;
import org.inspirenxe.skills.command.SkillsCommands;
import org.inspirenxe.skills.config.ConfigLoader;
import org.inspirenxe.skills.config.SkillDefinition;
import org.inspirenxe.skills.config.SkillsConfig;
import org.inspirenxe.skills.core.HolderListener;
import org.inspirenxe.skills.core.SkillServiceImpl;
import org.inspirenxe.skills.database.Database;
import org.inspirenxe.skills.feedback.Feedback;
import org.inspirenxe.skills.rule.RuleBook;
import org.inspirenxe.skills.rule.RuleListener;
import org.inspirenxe.skills.rule.RuleParser;
import org.inspirenxe.skills.rule.SmeltingTracker;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StartingEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Plugin(Skills.ID)
public final class SkillsPlugin {

    private final PluginContainer container;
    private final Logger logger;
    private final Path configDir;
    private final ConfigLoader configLoader;
    private final Feedback feedback;
    private final RuleListener ruleListener;
    private final SmeltingTracker smeltingTracker;

    private SkillsConfig config;
    private List<SkillDefinition> definitions = List.of();
    private Database database;
    private SkillServiceImpl service;
    private ScheduledTask saveTask;

    @Inject
    public SkillsPlugin(final PluginContainer container, final Logger logger, @ConfigDir(sharedRoot = false) final Path configDir) {
        this.container = container;
        this.logger = logger;
        this.configDir = configDir;
        this.configLoader = new ConfigLoader(logger, configDir);
        this.feedback = new Feedback(logger);
        this.ruleListener = new RuleListener(logger, this.feedback);
        this.smeltingTracker = new SmeltingTracker(logger, this.ruleListener, configDir);
    }

    @Listener
    public void onConstruct(final ConstructPluginEvent event) {
        Tasks.init(this.container);
        try {
            this.configLoader.writeDefaults();
            this.config = this.configLoader.loadMain();
            this.definitions = this.configLoader.loadSkills();
        } catch (final Exception e) {
            this.logger.error("Skills could not read its config in '{}' and stays disabled!", this.configDir, e);
            return;
        }
        if (this.definitions.isEmpty()) {
            this.logger.warn("No skills are defined in '{}'.", this.configDir.resolve(ConfigLoader.SKILLS_DIR));
        }
        Sponge.eventManager().registerListeners(this.container, new HolderListener(), MethodHandles.lookup());
        Sponge.eventManager().registerListeners(this.container, this.ruleListener, MethodHandles.lookup());
        Sponge.eventManager().registerListeners(this.container, this.smeltingTracker, MethodHandles.lookup());
        Sponge.eventManager().registerListeners(this.container, this.feedback, MethodHandles.lookup());
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        if (this.config == null) {
            return;
        }
        // The database exists once the server starts; commands only run after that
        event.register(this.container, new SkillsCommands(this.logger, () -> this.database, this::reloadRules).create(), "skills");
    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        if (this.config == null) {
            return;
        }
        final Database database = new Database(this.logger, this.config.databaseFile(), this.configDir.resolve("libs"));
        try {
            database.open();
        } catch (final Exception e) {
            this.logger.error("Skills could not open its database '{}' and stays disabled!", this.config.databaseFile(), e);
            return;
        }
        this.database = database;
        final List<SkillType> types = new ArrayList<>();
        for (final SkillDefinition definition : this.definitions) {
            types.add(definition.type());
        }
        this.service = new SkillServiceImpl(this.logger, database, types, this.config.shares());
        Skills.setService(this.service);
        this.applyRules(this.definitions);
        // Worlds loaded before this point get their container here, later ones on LoadWorldEvent
        for (final ServerWorld world : Sponge.server().worldManager().worlds()) {
            this.service.createContainer(world.key());
        }
        this.logger.info("Skills enabled with {} skills.", types.size());
    }

    @Listener
    public void onServerStarted(final StartedEngineEvent<Server> event) {
        if (this.service == null) {
            return;
        }
        for (final ServerWorld world : Sponge.server().worldManager().worlds()) {
            this.service.createContainer(world.key());
        }
        HolderListener.loadOnline();
        this.saveTask = Tasks.repeat("Skills - Save", Duration.ofSeconds(this.config.saveIntervalSeconds()), this.service::saveAll);
        this.smeltingTracker.start();
    }

    @Listener
    public void onRefresh(final RefreshGameEvent event) {
        this.reloadRules();
    }

    @Listener
    public void onServerStopping(final StoppingEngineEvent<Server> event) {
        if (this.service == null) {
            return;
        }
        if (this.saveTask != null) {
            this.saveTask.cancel();
        }
        this.smeltingTracker.stop();
        // From here on every save waits for its write
        this.database.setStopping();
        this.service.saveAll();
        this.database.close();
        Skills.setService(null);
        this.service = null;
    }

    private void reloadRules() {
        if (this.service == null) {
            return;
        }
        this.definitions = this.configLoader.reloadRules(this.definitions);
        this.applyRules(this.definitions);
    }

    private void applyRules(final List<SkillDefinition> definitions) {
        final RuleParser parser = new RuleParser(this.logger);
        final RuleBook book = new RuleBook();
        final Map<SkillType, FireworkEffect> fireworks = new HashMap<>();
        for (final SkillDefinition definition : definitions) {
            final int rules = parser.parse(definition, book);
            parser.firework(definition).ifPresent(effect -> fireworks.put(definition.type(), effect));
            this.logger.info("Loaded {} rules for skill '{}'.", rules, definition.type().name());
        }
        this.ruleListener.setRules(book);
        this.feedback.setFireworks(fireworks);
    }
}
