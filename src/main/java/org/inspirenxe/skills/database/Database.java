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
package org.inspirenxe.skills.database;

import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The experience database: one H2 2 file holding the experience of every holder per skill and container (world key).
 *
 * <p>All database work runs on one dedicated thread, in submission order, so a load queued after a save always sees that save.</p>
 */
public final class Database {

    private static final String SCHEMA = "CREATE TABLE IF NOT EXISTS SKILLS_EXPERIENCE ("
            + "CONTAINER VARCHAR(200) NOT NULL, "
            + "HOLDER UUID NOT NULL, "
            + "SKILL VARCHAR(200) NOT NULL, "
            + "EXPERIENCE DOUBLE PRECISION DEFAULT 0 NOT NULL, "
            + "CREATED TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, "
            + "MODIFIED TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, "
            + "PRIMARY KEY (CONTAINER, HOLDER, SKILL))";

    private final Logger logger;
    private final Path file;
    private final Path libDir;
    private ExecutorService executor;
    private Connection connection;
    private volatile boolean stopping;

    /**
     * @param file The database file without its extension (H2 adds {@code .mv.db})
     * @param libDir Where the bundled H2 jar is unpacked to
     */
    public Database(final Logger logger, final Path file, final Path libDir) {
        this.logger = logger;
        this.file = file.toAbsolutePath().normalize();
        this.libDir = libDir;
    }

    public synchronized void open() throws Exception {
        if (this.executor != null) {
            return;
        }
        final Driver driver = H2Driver.load(this.libDir);
        final Connection connection = driver.connect("jdbc:h2:file:" + this.file.toString().replace('\\', '/'), new Properties());
        if (connection == null) {
            throw new SQLException("The H2 driver refused the database path '" + this.file + "'");
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(Database.SCHEMA);
        } catch (final SQLException e) {
            connection.close();
            throw e;
        }
        this.connection = connection;
        this.stopping = false;
        this.executor = Executors.newSingleThreadExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "Skills - Database");
            thread.setDaemon(true);
            return thread;
        });
        this.logger.info("Opened the Skills database '{}.mv.db'.", this.file);
    }

    /**
     * True while the server stops: saves then wait for their write so nothing is lost.
     */
    public boolean isStopping() {
        return this.stopping;
    }

    public void setStopping() {
        this.stopping = true;
    }

    /**
     * Runs database work on the database thread.
     */
    public CompletableFuture<Void> execute(final Runnable runnable) {
        final ExecutorService executor = this.executor;
        if (executor == null || executor.isShutdown()) {
            return CompletableFuture.failedFuture(new IllegalStateException("The Skills database is closed!"));
        }
        return CompletableFuture.runAsync(runnable, executor);
    }

    // Everything below must run on the database thread

    public OptionalDouble fetch(final String container, final UUID holder, final String skill) throws SQLException {
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT EXPERIENCE FROM SKILLS_EXPERIENCE WHERE CONTAINER = ? AND HOLDER = ? AND SKILL = ?")) {
            statement.setString(1, container);
            statement.setObject(2, holder);
            statement.setString(3, skill);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? OptionalDouble.of(result.getDouble(1)) : OptionalDouble.empty();
            }
        }
    }

    /**
     * @return Skill key to experience, for every skill the holder has a row for
     */
    public Map<String, Double> fetchAll(final String container, final UUID holder) throws SQLException {
        final Map<String, Double> experience = new HashMap<>();
        try (PreparedStatement statement = this.connection.prepareStatement(
                "SELECT SKILL, EXPERIENCE FROM SKILLS_EXPERIENCE WHERE CONTAINER = ? AND HOLDER = ?")) {
            statement.setString(1, container);
            statement.setObject(2, holder);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    experience.put(result.getString(1), result.getDouble(2));
                }
            }
        }
        return experience;
    }

    public void store(final String container, final UUID holder, final String skill, final double experience) throws SQLException {
        try (PreparedStatement update = this.connection.prepareStatement(
                "UPDATE SKILLS_EXPERIENCE SET EXPERIENCE = ?, MODIFIED = CURRENT_TIMESTAMP WHERE CONTAINER = ? AND HOLDER = ? AND SKILL = ?")) {
            update.setDouble(1, experience);
            update.setString(2, container);
            update.setObject(3, holder);
            update.setString(4, skill);
            if (update.executeUpdate() > 0) {
                return;
            }
        }
        try (PreparedStatement insert = this.connection.prepareStatement(
                "INSERT INTO SKILLS_EXPERIENCE (CONTAINER, HOLDER, SKILL, EXPERIENCE) VALUES (?, ?, ?, ?)")) {
            insert.setString(1, container);
            insert.setObject(2, holder);
            insert.setString(3, skill);
            insert.setDouble(4, experience);
            insert.executeUpdate();
        }
    }

    /**
     * Waits for the queued work, then closes the database.
     */
    public synchronized void close() {
        final ExecutorService executor = this.executor;
        if (executor == null) {
            return;
        }
        this.stopping = true;
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                this.logger.warn("The Skills database did not finish its work within 30 seconds, closing it anyway.");
                executor.shutdownNow();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        try {
            this.connection.close();
        } catch (final SQLException e) {
            this.logger.error("Failed to close the Skills database!", e);
        }
        this.executor = null;
        this.connection = null;
    }
}
