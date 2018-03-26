/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE <https://github.com/InspireNXE/>
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
package org.inspirenxe.skills.impl;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.kyori.membrane.facet.internal.Facets;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.inspirenxe.skills.api.level.LevelFunction;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.command.SkillsCommandCreator;
import org.inspirenxe.skills.impl.config.ConfigurationAdapter;
import org.inspirenxe.skills.impl.config.ServerConfiguration;
import org.inspirenxe.skills.impl.database.DatabaseManager;
import org.inspirenxe.skills.impl.registry.LevelFunctionRegistryModule;
import org.inspirenxe.skills.impl.registry.SkillTypeRegistryModule;
import org.inspirenxe.skills.impl.skill.SkillManagerImpl;
import org.inspirenxe.skills.impl.skill.SkillTypeBuilderImpl;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import java.nio.file.Path;
import java.sql.SQLException;

@Plugin(id = Constants.Plugin.ID)
public class SkillsImpl {

    public static SkillsImpl instance;

    @Inject
    public PluginContainer container;

    @Inject
    public Logger logger;

    @Inject
    @AsynchronousExecutor
    public SpongeExecutorService asyncExecutor;

    // TODO Need Kashike's fancy system
    public ConfigurationAdapter<ServerConfiguration> configAdapter;
    public DatabaseManager databaseManager;
    public SkillManagerImpl skillManager;

    @Inject
    private Injector injector;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path configPath;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;

    @Listener
    public void onGameConstruction(GameConstructionEvent event) {
        instance = this;

        this.injector.createChildInjector(new ServerModule());
        this.injector.getInstance(Facets.class).enable();

        Sponge.getRegistry().registerModule(LevelFunction.class, new LevelFunctionRegistryModule());
        Sponge.getRegistry().registerModule(SkillType.class, new SkillTypeRegistryModule());

        Sponge.getRegistry().registerBuilderSupplier(SkillType.Builder.class, SkillTypeBuilderImpl::new);

        this.configAdapter = new ConfigurationAdapter<>(ServerConfiguration.class, this.configPath, this.configLoader);
        this.databaseManager = new DatabaseManager(this.container, this.configAdapter.getConfig().database);
        try {
            this.databaseManager.createDataSource();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.skillManager = new SkillManagerImpl();
    }

    @Listener
    public void onGamePreInititialization(GamePreInitializationEvent event) {
        Sponge.getCommandManager().register(this.container, SkillsCommandCreator.createRootCommand(), "skills");
    }
}
