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

import com.almuradev.droplet.parser.Parser;
import com.google.inject.Inject;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import org.jooq.SQLDialect;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class DatabaseConfigurationParser implements Parser<DatabaseConfiguration> {

  @Inject
  private Parser<SQLDialect> dialectParser;
  @Inject
  private Parser<String> stringParser;
  @Inject
  private Parser<Integer> intParser;

  @Override
  public DatabaseConfiguration throwingParse(Node node) throws XMLException {
    SQLDialect dialect = this.dialectParser.parse(node.requireAttribute("dialect"));
    final String[] server = new String[]{"localhost"};
    final int[] port = new int[]{3306};
    Path path;
    String connectionString;
    String initialCatalog;

    if (dialect == SQLDialect.H2 || dialect == SQLDialect.MYSQL || dialect == SQLDialect.SQLITE) {
      node.attribute("server").ifPresent(attribute -> server[0] = this.stringParser.parse(attribute));
      node.attribute("port").ifPresent(attribute -> port[0] = this.intParser.parse(attribute));

      if (dialect == SQLDialect.H2 || dialect == SQLDialect.SQLITE) {
        path = Paths.get(this.stringParser.parse(node.nodes("path").findFirst().orElse(null)));

        connectionString = String.format("jdbc:%s:%s", dialect.getName().toLowerCase(), path);
        initialCatalog = null;
      } else {
        path = null;
        initialCatalog = this.stringParser.parse(node.requireAttribute("initial_catalog"));
        connectionString = String.format("jdbc:%s://%s:%d/%s", dialect.getName().toLowerCase(), server[0], port[0], initialCatalog);
      }
    } else {
      throw new UnsupportedOperationException("Only H2, SQLite, and MySQL are currently supported!");
    }

    return new DatabaseConfiguration(dialect, path, server[0], port[0], connectionString, initialCatalog);
  }
}
