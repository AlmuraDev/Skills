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
package org.inspirenxe.skills.impl;

import com.almuradev.droplet.DropletModule;
import com.almuradev.droplet.parser.Parser;
import com.almuradev.toolbox.inject.ToolboxBinder;
import com.google.inject.Provides;
import net.kyori.violet.AbstractModule;
import net.kyori.xml.node.Node;
import org.inspirenxe.skills.api.SkillManager;
import org.inspirenxe.skills.impl.command.SkillsCommandCreator;
import org.inspirenxe.skills.impl.component.ComponentModule;
import org.inspirenxe.skills.impl.configuration.ForConfiguration;
import org.inspirenxe.skills.impl.content.ContentModule;
import org.inspirenxe.skills.impl.database.DatabaseConfiguration;
import org.inspirenxe.skills.impl.database.DatabaseManager;
import org.inspirenxe.skills.impl.parser.ParserModule;
import org.inspirenxe.skills.impl.registry.RegistryModule;
import org.inspirenxe.skills.impl.skill.SkillHolderImpl;
import org.inspirenxe.skills.impl.skill.SkillImpl;
import org.inspirenxe.skills.impl.skill.SkillManagerImpl;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.AsynchronousExecutor;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.sql.SqlService;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Singleton;

public final class SkillsModule extends AbstractModule implements ToolboxBinder {

  @Override
  protected void configure() {

    this.bind(SkillManager.class).to(SkillManagerImpl.class);

    // Register content loader
    this.install(new DropletModule());
    this.install(new ContentModule());
    this.install(new RegistryModule());
    this.install(new ParserModule());
    this.install(new ComponentModule());

    // Register factories (for assisted injections)
    this.installFactory(SkillHolderImpl.Factory.class);
    this.installFactory(SkillImpl.Factory.class);

    // Register command tree
    this.command().rootProvider(SkillsCommandCreator.class, SkillsImpl.ID);

    this.facet().add(SkillManagerImpl.class);
  }

  @ForConfiguration
  @Provides
  @Singleton
  Node configuration(@ConfigDir(sharedRoot = false) final Path configDir) throws IOException, JDOMException {
    final SAXBuilder sb = new SAXBuilder();
    return Node.of(sb.build(configDir.resolve("skills.xml").toFile()).getRootElement());
  }

  @Provides
  @Singleton
  DatabaseManager database(final PluginContainer container, final ServiceManager serviceManager, @ForConfiguration final Node node,
      final Parser<DatabaseConfiguration> parser) {
    final Node databaseNode = node.elements("database")
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("database configuration not found"));
    return new DatabaseManager(container, serviceManager.provideUnchecked(SqlService.class), parser.parse(databaseNode));
  }

  @Provides
  @Singleton
  SkillManagerImpl skillManager(final PluginContainer container, final DatabaseManager databaseManager,
    @AsynchronousExecutor final SpongeExecutorService executor, final SkillHolderImpl.Factory factory) {
    return new SkillManagerImpl(container, databaseManager, executor, factory);
  }
}
