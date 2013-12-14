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
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

public class SQLDatabaseConnection implements DatabaseConnection {

    private final SQLConnectionInfo connectionInfo;
    private final Logger logger;
    private final Plugin plugin;
    private Connection connection;

    public SQLDatabaseConnection(SQLConnectionInfo connectionInfo, Logger logger, Plugin plugin) throws SQLException {
        this.connectionInfo = connectionInfo;
        this.logger = logger;
        this.plugin = plugin;
        try {
            connection = connectionInfo.createConnection();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Failed to create connection to `" + connectionInfo.getUrl() + "`", ex);
            throw ex;
        }
    }

    @Override
    public MapTable<String> getStringTable(@NonNull String name) {
        StringTable table = new StringTable(name);
        table.create();
        return table;
    }

    @Override
    public MapTable<Integer> getIntTable(@NonNull String name) {
        IntTable table = new IntTable(name);
        table.create();
        return table;
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private class StringTable implements MapTable<String> {

        private final String name;

        private void create() {
        }

        @Override
        public void get(String key, ResultRunnable<String> runWithResult) {

        }

        @Override
        public void set(String key, String value, ResultRunnable<Boolean> runAfter) {

        }
    }

    @RequiredArgsConstructor
    private class IntTable implements MapTable<Integer> {

        private final String name;

        private void create() {
        }

        @Override
        public void get(String key, ResultRunnable<Integer> runWithResult) {

        }

        @Override
        public void set(String key, Integer value, ResultRunnable<Boolean> runAfter) {

        }
    }
}
