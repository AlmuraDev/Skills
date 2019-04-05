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

import com.almuradev.toolbox.inject.event.Witness;
import com.almuradev.toolbox.inject.event.WitnessScope;
import com.google.common.base.Charsets;
import com.google.inject.Singleton;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.configuration.database.DatabaseConfiguration;
import org.jooq.DSLContext;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.sql.SqlService;

import java.net.URI;
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
    private final ServiceManager manager;

    @Nullable private DataSource dataSource;
    @Nullable private DataSource connectionSource;

    public DatabaseManager(final PluginContainer container, final ServiceManager manager, final DatabaseConfiguration configuration) {
        this.container = container;
        this.manager = manager;
        this.configuration = configuration;
    }

    public DataSource getOrCreateDataSource(final boolean includeSchema) throws SQLException {
        boolean existingDataSource = includeSchema ? this.dataSource != null : this.connectionSource != null;

        if (existingDataSource) {
            Connection connection = null;

            try {
                if (includeSchema) {
                    connection = this.dataSource.getConnection();
                } else {
                    connection = this.connectionSource.getConnection();
                }
            } catch (final SQLException ignored) {
                existingDataSource = false;
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }

        if (!existingDataSource) {
            if (includeSchema) {
                this.dataSource =
                    this.manager.provideUnchecked(SqlService.class).getDataSource(this.container, this.configuration.getConnectionString());
            } else {
                this.connectionSource = this.manager.provideUnchecked(SqlService.class)
                    .getDataSource(this.container, this.configuration.getConnectionStringWithoutSchema());
            }
        }

        return includeSchema ? this.dataSource : this.connectionSource;
    }

    public DSLContext createContext(final boolean includeSchema) throws SQLException {
        final DataSource dataSource = this.getOrCreateDataSource(includeSchema);

        return DSL.using(dataSource, this.configuration.getDialect(), new Settings().withRenderSchema(false).withRenderCatalog(false));
    }

    public DatabaseConfiguration getConfiguration() {
        return this.configuration;
    }

    @Listener(order = Order.FIRST)
    public void onGameAboutToStartServer(final GameAboutToStartServerEvent event) {
        try (final DSLContext context = this.createContext(false)) {
            context.createSchemaIfNotExists(this.getConfiguration().getInitialCatalog()).execute();
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }

        try (final DSLContext context = this.createContext(true)) {
            final URI uri = SkillsImpl.class.getResource("/db/database.sql").toURI();

            final Path path;

            if (uri.getScheme().equals("jar")) {
                final FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                path = fileSystem.getPath("/db/database.sql");
            } else {
                path = Paths.get(uri);
            }

            if (path != null) {
                final byte[] encoded;
                encoded = Files.readAllBytes(path);
                final String sql = new String(encoded, Charsets.UTF_8);
                context.execute(sql);
            }
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
