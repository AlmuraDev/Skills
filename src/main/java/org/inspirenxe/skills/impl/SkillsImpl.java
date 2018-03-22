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
