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

import org.jooq.SQLDialect;

import java.nio.file.Path;

import javax.annotation.Nullable;

public final class DatabaseConfiguration {

  private final SQLDialect dialect;
  @Nullable private final Path path;
  private final String server;
  private final int port;
  @Nullable private final String initialCatalog;
  private final String connectionString;

  DatabaseConfiguration(final SQLDialect dialect, @Nullable final Path path, final String server, final int port, @Nullable final String initialCatalog,
    final String connectionString) {
    this.dialect = dialect;
    this.path = path;
    this.server = server;
    this.port = port;
    this.initialCatalog = initialCatalog;
    this.connectionString = connectionString;
  }

  public SQLDialect getDialect() {
    return this.dialect;
  }

  @Nullable
  public Path getPath() {
    return this.path;
  }

  public String getServer() {
    return this.server;
  }

  public int getPort() {
    return this.port;
  }

  @Nullable
  public String getInitialCatalog() {
    return this.initialCatalog;
  }

  public String getConnectionString() {
    return this.connectionString;
  }
}
