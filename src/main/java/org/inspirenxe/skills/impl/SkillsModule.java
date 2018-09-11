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

import com.almuradev.toolbox.inject.ToolboxBinder;
import com.almuradev.toolbox.inject.command.CommandInstaller;
import com.almuradev.toolbox.inject.event.WitnessModule;
import com.almuradev.toolbox.inject.registry.RegistryInstaller;
import com.google.inject.Provides;
import net.kyori.violet.AbstractModule;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.inspirenxe.skills.impl.command.SkillsCommandCreator;
import org.inspirenxe.skills.impl.configuration.ForConfiguration;
import org.inspirenxe.skills.impl.content.ContentModule;
import org.inspirenxe.skills.impl.database.DatabaseConfiguration;
import org.inspirenxe.skills.impl.database.DatabaseManager;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ServiceManager;

import java.io.IOException;
import java.nio.file.Path;

import javax.inject.Singleton;

public final class SkillsModule extends AbstractModule implements ToolboxBinder {

  @Override
  protected void configure() {

    this.install(new WitnessModule());
    this.install(new ContentModule());

    // Register factories (for assisted injections)
    this.installFactory(SkillServiceImpl.Factory.class);
    this.installFactory(SkillHolderImpl.Factory.class);
    this.installFactory(SkillImpl.Factory.class);

    // Register command tree
    this.command().rootProvider(SkillsCommandCreator.class, SkillsImpl.ID);

    this.facet()
      .add(CommandInstaller.class)
      .add(RegistryInstaller.class)
      .add(DatabaseManager.class)
      .add(SkillLoader.class);
  }

  @ForConfiguration
  @Provides
  @Singleton
  Node configuration(@ConfigDir(sharedRoot = false) final Path configDir) throws IOException, JDOMException {
    final SAXBuilder sb = new SAXBuilder();
    return Node.of(sb.build(configDir.resolve(SkillsImpl.ID + ".xml").toFile()).getRootElement());
  }

  @Provides
  @Singleton
  DatabaseManager database(final PluginContainer container, final ServiceManager serviceManager, @ForConfiguration final Node node,
    final Parser<DatabaseConfiguration> parser) {
    final Node databaseNode = node.elements("database")
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("database configuration not found"));
    return new DatabaseManager(container, serviceManager, parser.parse(databaseNode));
  }
}
