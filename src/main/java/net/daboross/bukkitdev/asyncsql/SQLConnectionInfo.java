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
package net.daboross.bukkitdev.asyncsql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.lang.Validate;

public class SQLConnectionInfo {

    private final String url;
    private final Properties properties;

    public SQLConnectionInfo(String host, int port, String database, String username, String password) {
        Validate.notNull(host, "Host cannot be null");
        Validate.notNull(database, "Database cannot be null");
        Validate.notNull(username, "Username cannot be null");
        Validate.notNull(password, "Password cannot be null");
        this.url = String.format("jdbc:mysql://%s:%s/%s", host, port, database);
        this.properties = new Properties();
        this.properties.setProperty("user", username);
        this.properties.setProperty("password", password);
    }

    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection(url, properties);
    }

    public String getUrl() {
        return url;
    }

    public void setProperty(String key, String value) {
        this.properties.setProperty(key, value);
    }
}
