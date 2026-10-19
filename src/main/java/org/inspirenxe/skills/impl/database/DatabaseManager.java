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
import com.google.common.io.Resources;
import com.google.inject.Singleton;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.configuration.database.DatabaseConfiguration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.sql.SqlService;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    @Nullable private ExecutorService executor;
    private volatile boolean stopping;

    public DatabaseManager(final PluginContainer container, final ServiceManager manager, final DatabaseConfiguration configuration) {
        this.container = container;
        this.manager = manager;
        this.configuration = configuration;
    }

    public synchronized DataSource getOrCreateDataSource(final boolean includeSchema) throws SQLException {
        DataSource source = includeSchema ? this.dataSource : this.connectionSource;

        if (source == null) {
            source = this.manager.provideUnchecked(SqlService.class).getDataSource(this.container, includeSchema ?
                this.configuration.getConnectionString() : this.configuration.getConnectionStringWithoutSchema());

            // Test once, only cache a working data source
            try (final Connection ignored = source.getConnection()) {
                if (includeSchema) {
                    this.dataSource = source;
                } else {
                    this.connectionSource = source;
                }
            }
        }

        return source;
    }

    public DSLContext createContext(final boolean includeSchema) throws SQLException {
        final DataSource dataSource = this.getOrCreateDataSource(includeSchema);

        return DSL.using(dataSource, this.configuration.getDialect(), new Settings().withRenderSchema(false).withRenderCatalog(false));
    }

    public DatabaseConfiguration getConfiguration() {
        return this.configuration;
    }

    /**
     * Runs database work on a single dedicated thread so loads and saves execute in submission order.
     */
    public CompletableFuture<Void> execute(final Runnable runnable) {
        return CompletableFuture.runAsync(runnable, this.getExecutor());
    }

    /**
     * True once the server has begun stopping; database work should then be waited upon.
     */
    public boolean isStopping() {
        return this.stopping;
    }

    private synchronized ExecutorService getExecutor() {
        if (this.executor == null || this.executor.isShutdown()) {
            this.executor = Executors.newSingleThreadExecutor(r -> {
                final Thread thread = new Thread(r, "Skills Database");
                thread.setDaemon(true);
                return thread;
            });
        }

        return this.executor;
    }

    @Listener(order = Order.FIRST)
    public void onGameStartingServer(final GameStartingServerEvent event) {
        this.stopping = false;

        final boolean postgres = this.configuration.getDialect().family() == SQLDialect.POSTGRES;

        try (final DSLContext context = this.createContext(false)) {
            final String catalog = this.configuration.getInitialCatalog();

            if (postgres) {
                final boolean exists = context.fetchExists(DSL.selectOne().from(DSL.table(DSL.name("pg_database")))
                    .where(DSL.field(DSL.name("datname")).eq(catalog)));

                if (!exists) {
                    context.execute("create database " + context.render(DSL.name(catalog)));
                }
            } else {
                context.createSchemaIfNotExists(catalog).execute();
            }
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to create database/schema '" + this.configuration.getInitialCatalog() + "' (dialect "
                + this.configuration.getDialect() + ")!", ex);
        }

        final String script = postgres ? "/db/database-postgres.sql" : "/db/database.sql";

        try (final DSLContext context = this.createContext(true)) {
            final URL url = SkillsImpl.class.getResource(script);
            if (url == null) {
                throw new IllegalStateException("Missing database script " + script);
            }

            context.execute(Resources.toString(url, Charsets.UTF_8));
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to execute database script " + script + " (dialect " + this.configuration.getDialect() + ")!", ex);
        }
    }

    @Listener(order = Order.FIRST)
    public void onGameStoppingServer(final GameStoppingServerEvent event) {
        this.stopping = true;
    }

    @Listener(order = Order.LAST)
    public void onGameStoppedServer(final GameStoppedServerEvent event) {
        final ExecutorService executor;

        synchronized (this) {
            executor = this.executor;
            this.executor = null;
        }

        if (executor == null) {
            return;
        }

        executor.shutdown();

        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
