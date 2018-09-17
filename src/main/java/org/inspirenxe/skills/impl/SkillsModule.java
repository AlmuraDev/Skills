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
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import net.kyori.violet.AbstractModule;
import net.kyori.violet.FriendlyTypeLiteral;
import net.kyori.violet.TypeArgument;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.EnumParser;
import net.kyori.xml.node.parser.Parser;
import net.kyori.xml.node.parser.ParserBinder;
import org.inspirenxe.skills.impl.command.SkillsCommandProvider;
import org.inspirenxe.skills.impl.configuration.ForConfiguration;
import org.inspirenxe.skills.impl.configuration.PluginConfiguration;
import org.inspirenxe.skills.impl.configuration.PluginConfigurationParser;
import org.inspirenxe.skills.impl.configuration.container.ContainerShareConfiguration;
import org.inspirenxe.skills.impl.configuration.container.ContainerShareConfigurationParser;
import org.inspirenxe.skills.impl.configuration.database.DatabaseConfigurationParser;
import org.inspirenxe.skills.impl.content.ContentModule;
import org.inspirenxe.skills.impl.configuration.database.DatabaseConfiguration;
import org.inspirenxe.skills.impl.content.parser.value.PrimitiveStringToValueParser;
import org.inspirenxe.skills.impl.content.parser.value.StringToValueParser;
import org.inspirenxe.skills.impl.database.DatabaseManager;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jooq.SQLDialect;
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
    this.installFactory(SkillHolderContainerImpl.Factory.class);
    this.installFactory(SkillHolderImpl.Factory.class);
    this.installFactory(SkillImpl.Factory.class);

    final ParserBinder parsers = new ParserBinder(this.binder());
    parsers.bindParser(PluginConfiguration.class).to(PluginConfigurationParser.class);
    parsers.bindParser(SQLDialect.class).to(new TypeLiteral<EnumParser<SQLDialect>>() {});
    parsers.bindParser(DatabaseConfiguration.class).to(DatabaseConfigurationParser.class);
    parsers.bindParser(ContainerShareConfiguration.class).to(ContainerShareConfigurationParser.class);
    this.bindRawParser(Boolean.class).to(new TypeLiteral<PrimitiveStringToValueParser<Boolean>>() {});
    this.bindRawParser(String.class).to(new TypeLiteral<PrimitiveStringToValueParser<String>>() {});
    this.bindRawParser(Integer.class).to(new TypeLiteral<PrimitiveStringToValueParser<Integer>>() {});

    // Register command tree
    this.command().rootProvider(SkillsCommandProvider.class, SkillsImpl.ID);

    this.facet()
      .add(CommandInstaller.class)
      .add(RegistryInstaller.class)
      .add(DatabaseManager.class)
      .add(SkillLoader.class);
  }

  private <T> LinkedBindingBuilder<StringToValueParser<T>> bindRawParser(final Class<T> type) {
    return this.bind(new FriendlyTypeLiteral<StringToValueParser<T>>() {}.where(new TypeArgument<T>(type) {}));
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
  PluginConfiguration pluginConfiguration(@ForConfiguration final Node node, final Parser<PluginConfiguration> parser) {
    return parser.parse(node);
  }

  @Provides
  @Singleton
  DatabaseManager database(final PluginContainer container, final ServiceManager serviceManager, final PluginConfiguration pluginConfiguration) {
    return new DatabaseManager(container, serviceManager, pluginConfiguration.getDatabaseConfiguration());
  }
}
