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
