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
package org.inspirenxe.skills.impl.database;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.toolbox.inject.event.Witness;
import com.almuradev.toolbox.inject.event.WitnessScope;
import com.google.common.base.Charsets;
import com.google.inject.Singleton;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.generated.Tables;
import org.inspirenxe.skills.generated.tables.SkillExperience;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.jooq.DSLContext;
import org.jooq.SQL;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.sql.SqlService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import javax.annotation.Nullable;
import javax.sql.DataSource;

@Singleton
@WitnessScope.Sponge
public final class DatabaseManager implements Witness {

  private final PluginContainer container;
  private final DatabaseConfiguration configuration;
  private SqlService service;

  @Nullable private DataSource dataSource;

  public DatabaseManager(final PluginContainer container, final SqlService service, final DatabaseConfiguration configuration) {
    this.container = container;
    this.service = service;
    this.configuration = configuration;
  }

  public DataSource getOrCreateDataSource() throws SQLException {
    if (this.dataSource == null) {
      this.dataSource = this.service.getDataSource(this.container, this.configuration.getConnectionString());
    }

    return this.dataSource;
  }

  public DataSource getDataSource() {
    checkNotNull(this.dataSource, "Data Source has not been initialized yet! (Did you forget to call getOrCreateDataSource()?)");
    return this.dataSource;
  }

  public Connection getConnection() throws SQLException {
    checkNotNull(this.getDataSource(), "Data Source has not been initialized yet! (Did you forget to call getOrCreateDataSource()?)");
    return this.getDataSource().getConnection();
  }

  public DSLContext createContext() {
    try {
      this.getOrCreateDataSource();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return DSL.using(this.getDataSource(), this.configuration.getDialect());
  }

  public DatabaseConfiguration getConfiguration() {
    return this.configuration;
  }

  @Listener(order = Order.AFTER_PRE)
  public void onGamePreInitialization(GamePreInitializationEvent event) {
    try (final DSLContext context = DSL.using(this.service.getDataSource(this.configuration.getConnectionStringNoSchema()), this.configuration
      .getDialect())) {
      context.createSchemaIfNotExists(this.configuration.getInitialCatalog()).execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    try (final DSLContext context = this.createContext()) {
      final URI uri = SkillsImpl.class.getResource("/database.sql").toURI();

      Path path;

      if (uri.getScheme().equals("jar")) {
        final FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
        path = fileSystem.getPath("/database.sql");
      } else {
        path = Paths.get(uri);
      }

      if (path != null) {
        final byte[] encoded;
        encoded = Files.readAllBytes(path);
        final String sql = new String(encoded, Charsets.UTF_8);
        context.execute(sql);
      }
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
    }
  }

  @Listener(order = Order.LAST)
  public void onServiceChange(ChangeServiceProviderEvent event) {
    if (event.getService() == SqlService.class) {
      this.service = (SqlService) event.getNewProvider();

      try {
        this.getOrCreateDataSource();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
