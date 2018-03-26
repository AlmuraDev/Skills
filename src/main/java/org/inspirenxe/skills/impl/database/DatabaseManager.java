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
package org.inspirenxe.skills.impl.database;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.impl.config.category.DatabaseCategory;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.sql.SqlService;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

public final class DatabaseManager {

    private final PluginContainer pluginContainer;
    private final DatabaseCategory databaseCategory;
    private final String targetUrl;
    private SqlService sqlService;
    private DataSource dataSource;

    public DatabaseManager(final PluginContainer pluginContainer, final DatabaseCategory databaseCategory) {
        this.pluginContainer = pluginContainer;
        this.databaseCategory = databaseCategory;

        switch (this.databaseCategory.connector) {
            // TODO Should support remote H2 databases but low priority.
            case H2:
            case SQLITE:
                // TODO Sql Service in Sponge should resolve this against the plugin's config directory like it does with H2..
                this.targetUrl = String.format("jdbc:%s:%s", databaseCategory.connector.getName().toLowerCase(), databaseCategory.database);
                break;
            case MYSQL:
                this.targetUrl = String.format("jdbc:%s://%s:%d/%s", databaseCategory.connector.getName().toLowerCase(), databaseCategory.server,
                        databaseCategory.port, databaseCategory.database);
                break;
            default:
                throw new UnsupportedOperationException("Only H2, SQLite and MySQL are currently supported via this manager!");
        }

        this.sqlService = Sponge.getServiceManager().provide(SqlService.class).get();

        // TODO Change this to use kashike's fancy system
        Sponge.getEventManager().registerListeners(pluginContainer, this);
    }

    public void createDataSource() throws SQLException {
        this.dataSource = this.sqlService.getDataSource(this.pluginContainer, this.targetUrl);
    }

    public DataSource getDataSource() {
        checkNotNull(this.dataSource, "Data Source has not been initialized yet! (Did you forget to call createDataSource()?)");
        return this.dataSource;
    }

    public Connection getConnection() throws SQLException {
        checkNotNull(this.dataSource, "Data Source has not been initialized yet! (Did you forget to call createDataSource()?)");
        return this.dataSource.getConnection();
    }

    public DSLContext createContext() throws SQLException {
        return DSL.using(this.dataSource, this.databaseCategory.connector);
    }

    @Listener (order = Order.LAST)
    public void onServiceChange(ChangeServiceProviderEvent event) {
        if (event.getService() == SqlService.class) {
            this.sqlService = (SqlService) event.getNewProvider();

            try {
                this.createDataSource();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
