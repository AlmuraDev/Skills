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

import com.google.common.collect.MoreCollectors;
import com.google.inject.Inject;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.jooq.SQLDialect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DatabaseConfigurationParser implements Parser<DatabaseConfiguration> {

  private final Parser<SQLDialect> dialectParser;
  private final Parser<String> stringParser;
  private final Parser<Integer> intParser;

  @Inject
  public DatabaseConfigurationParser(final Parser<SQLDialect> dialectParser, final Parser<String> stringParser, final Parser<Integer> intParser) {
    this.dialectParser = dialectParser;
    this.stringParser = stringParser;
    this.intParser = intParser;
  }

  @Override
  public DatabaseConfiguration throwingParse(Node node) throws XMLException {
    final SQLDialect dialect = this.dialectParser.parse(node.requireAttribute("dialect"));
    final String initialCatalog;
    final String server;
    final Integer port;

    Path path;
    String connectionString;
    String connectionStringNoSchema;

    if (dialect == SQLDialect.H2 || dialect == SQLDialect.POSTGRES_9_5) {
      initialCatalog = this.stringParser.parse(node.requireAttribute("initial-catalog"));

      if (dialect == SQLDialect.H2) {
        path = Paths.get(this.stringParser.parse(node.nodes("path").collect(MoreCollectors.onlyElement())));
        server = null;
        port = null;

        Path fsPath = path;

        if (!Files.notExists(fsPath)) {
          fsPath = Paths.get(fsPath.toString() + ".mv.db");
        }

        if (Files.notExists(fsPath)) {
          try {
            Files.createFile(fsPath);
          } catch (IOException e) {
            throw new RuntimeException("Failed to create H2 database file! Path: " + fsPath);
          }
        }
        connectionStringNoSchema = String.format("jdbc:%s:%s;%s;%s", dialect.getName().toLowerCase(), path.toAbsolutePath(),
          "DATABASE_TO_UPPER=FALSE", "AUTO_SERVER=TRUE");

        connectionString = String.format("jdbc:%s:%s;%s;%s;%s", dialect.getName().toLowerCase(), path.toAbsolutePath(), "SCHEMA=" +
          initialCatalog, "DATABASE_TO_UPPER=FALSE", "AUTO_SERVER=TRUE");
      } else {
        server = this.stringParser.parse(node.requireAttribute("server"));
        port = this.intParser.parse(node.requireAttribute("port"));

        path = null;
        connectionStringNoSchema = String.format("jdbc:%s://%s:%d", dialect.getName().toLowerCase(), server, port);
        connectionString = String.format("jdbc:%s://%s:%d/%s", dialect.getName().toLowerCase(), server, port, initialCatalog);
      }
    } else {
      throw new UnsupportedOperationException("Only H2 or Postgres_9_5 are currently supported!");
    }

    return new DatabaseConfiguration(dialect, initialCatalog, path, server, port, connectionStringNoSchema, connectionString);
  }
}
