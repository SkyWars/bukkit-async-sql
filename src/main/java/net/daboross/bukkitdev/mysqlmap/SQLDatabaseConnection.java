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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daboross.bukkitdev.mysqlmap.api.DatabaseConnection;
import net.daboross.bukkitdev.mysqlmap.api.MapTable;
import net.daboross.bukkitdev.mysqlmap.api.ResultRunnable;
import net.daboross.bukkitdev.mysqlmap.internal.AsyncSQL;
import net.daboross.bukkitdev.mysqlmap.internal.ResultHolder;
import net.daboross.bukkitdev.mysqlmap.internal.ResultSQLRunnable;
import net.daboross.bukkitdev.mysqlmap.internal.SQLRunnable;
import org.bukkit.plugin.Plugin;

public class SQLDatabaseConnection implements DatabaseConnection {

    private final AsyncSQL sql;

    public SQLDatabaseConnection(Plugin plugin, Logger logger, SQLConnectionInfo connectionInfo) throws SQLException {
        this.sql = new AsyncSQL(plugin, logger, connectionInfo);
    }

    public SQLDatabaseConnection(Plugin plugin, SQLConnectionInfo connectionInfo) throws Exception {
        this(plugin, plugin.getLogger(), connectionInfo);
    }

    @Override
    public MapTable<String, String> getStringToStringTable(@NonNull String name) {
        StringTable table = new StringTable(name);
        table.create();
        return table;
    }

    @Override
    public MapTable<String, Integer> getStringToIntTable(@NonNull String name) {
        IntTable table = new IntTable(name);
        table.create();
        return table;
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private class StringTable implements MapTable<String, String> {

        private final String name;

        private void create() {
            sql.run("create StringTable " + name, new SQLRunnable() {
                @Override
                public void run(Connection connection) throws SQLException {
                    String query = String.format("CREATE TABLE IF NOT EXISTS `%s` (`stringKey` TEXT, `stringValue` TEXT, PRIMARY KEY (`stringKey`));", name);
                    PreparedStatement statement = connection.prepareStatement(query);
                    try {
                        statement.execute();
                    } finally {
                        statement.close();
                    }
                }
            });
        }

        @Override
        public void get(final String key, final ResultRunnable<String> runWithResult) {
            sql.run(String.format("get value %s from %s", key, name), new ResultSQLRunnable<String>() {
                @Override
                public void run(Connection connection, ResultHolder<String> result) throws SQLException {
                    String query = String.format("SELECT `stringValue` FROM `%s` WHERE `stringKey` = ?", name);
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, key);
                    try {
                        statement.execute();
                        ResultSet set = statement.executeQuery();
                        result.set(set.getString(1));
                    } finally {
                        statement.close();
                    }
                }
            }, runWithResult);
        }

        @Override
        public void set(final String key, final String value, final ResultRunnable<Boolean> runWithResult) {
            sql.run(String.format("set value %s to %s in %s", key, value, name), new ResultSQLRunnable<Boolean>() {
                @Override
                public void run(Connection connection, ResultHolder<Boolean> result) throws SQLException {
                    String query = String.format("REPLACE INTO `%s` (`stringKey`, `stringValue`) VALUES(?, ?)", name);
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, key);
                    statement.setString(2, value);
                    try {
                        result.set(statement.executeUpdate() != 0);
                    } finally {
                        statement.close();
                    }
                }
            }, runWithResult);
        }
    }

    @RequiredArgsConstructor
    private class IntTable implements MapTable<String, Integer> {

        private final String name;

        private void create() {
            sql.run("create IntTable " + name, new SQLRunnable() {
                @Override
                public void run(Connection connection) throws SQLException {
                    String query = String.format("CREATE TABLE IF NOT EXISTS `%s` (`stringKey` TEXT, `intValue` INT, PRIMARY KEY (`stringKey`))", name);
                    PreparedStatement statement = connection.prepareStatement(query);
                    try {
                        statement.execute();
                    } finally {
                        statement.close();
                    }
                }
            });
        }

        @Override
        public void get(final String key, final ResultRunnable<Integer> runWithResult) {
            sql.run(String.format("get value %s from %s", key, name), new ResultSQLRunnable<Integer>() {
                @Override
                public void run(Connection connection, ResultHolder<Integer> result) throws SQLException {
                    String query = String.format("SELECT `intValue` FROM `%s` WHERE `stringKey` = ?", name);
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, key);
                    try {
                        statement.execute();
                        ResultSet set = statement.executeQuery();
                        result.set(set.getInt(1));
                    } finally {
                        statement.close();
                    }
                }
            }, runWithResult);
        }

        @Override
        public void set(final String key, final Integer value, final ResultRunnable<Boolean> runWithResult) {
            sql.run(String.format("set value %s to %s in %s", key, value, name), new ResultSQLRunnable<Boolean>() {
                @Override
                public void run(Connection connection, ResultHolder<Boolean> result) throws SQLException {
                    String query = String.format("REPLACE INTO `%s` (`stringKey`, `intValue`) VALUES(?, ?)", name);
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, key);
                    statement.setInt(2, value);

                    try {
                        result.set(statement.executeUpdate() != 0);
                    } finally {
                        statement.close();
                    }
                }
            }, runWithResult);
        }
    }
}
