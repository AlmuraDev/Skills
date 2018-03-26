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
package org.inspirenxe.skills.impl.config.category;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.jooq.SQLDialect;

@ConfigSerializable
public final class DatabaseCategory {

    @Setting(value = "server", comment = "The server IP address that holds the SQL-based server instance. Ignored for H2/SQLite.")
    public String server = "localhost";

    @Setting(value = "port", comment = "The port to use when connecting to the IP address. Ignored for H2/SQLite.")
    public int port = 3306;

    @Setting(value = "database", comment = "The database to use on the target server. Specify as a filepath for H2/SQLite, relative to the game "
            + "directory.")
    public String database = "/config/skills/skills.db";

    @Setting(value = "user", comment = "The user to use when connecting to the database. Ignored for H2/SQLite.")
    public String user = "root";

    @Setting(value = "password", comment = "The password to use when connecting to the database. Ignored for H2/SQLite.")
    public String password = "";

    @Setting(value = "connector", comment = "The connection type to use.")
    public SQLDialect connector = SQLDialect.SQLITE;

    @Setting(value = "save_interval", comment = "Interval (in seconds) between saves to the database. Keep in mind that some database queries are "
            + "submitted immediately!")
    public long saveInterval = 60;
}
