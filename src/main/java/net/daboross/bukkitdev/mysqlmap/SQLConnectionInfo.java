/*
 * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.daboross.bukkitdev.mysqlmap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import lombok.Getter;
import lombok.NonNull;

public class SQLConnectionInfo {

    @Getter
    private final String url;
    private final Properties properties;

    public SQLConnectionInfo(@NonNull String host, int port, @NonNull String database, @NonNull String username, @NonNull String password) {
        this.url = String.format("jdbc:mysql://%s:%s/%s", host, port, database);
        this.properties = new Properties();
        this.properties.setProperty("user", username);
        this.properties.setProperty("password", password);
    }

    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, properties);
    }
}
