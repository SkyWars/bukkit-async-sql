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
    private final AsyncTaskScheduler taskScheduler;
    private Connection connection;

    public SQLDatabaseConnection(SQLConnectionInfo connectionInfo, Logger logger, Plugin plugin) throws SQLException {
        this.connectionInfo = connectionInfo;
        this.logger = logger;
        this.plugin = plugin;
        this.taskScheduler = new AsyncTaskScheduler(plugin, logger, "SQL Task Thread for " + connectionInfo.getUrl());
        try {
            connection = connectionInfo.createConnection();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Failed to create connection to `" + connectionInfo.getUrl() + "`", ex);
            throw ex;
        }
    }

    private void connectToSQL() throws SQLException {
        try {
            connection = connectionInfo.createConnection();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Failed to create connection to `" + connectionInfo.getUrl() + "`", ex);
            throw ex;
        }
    }

    private void runAsync(final String taskName, final SQLRunnable runnable) {
        taskScheduler.queueRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        runnable.run();
                    } catch (SQLException ex) {
                        try {
                            logger.log(Level.INFO, "Failed to " + taskName + ": " + ex.getMessage() + ". Reconnecting and retrying.");
                            connection.close();
                            connectToSQL();
                            runnable.run();
                        } catch (SQLException ex2) {
                            logger.log(Level.WARNING, "Failed to " + taskName + ", not retrying:", ex2);
                        }
                    }
                } catch (RuntimeException ex) {
                    throw new RuntimeException("Exception " + taskName + ":", ex);
                }
            }
        });
    }

    private <T> void runSync(final ResultRunnable<T> result, final T toPass) {
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                result.runWithResult(toPass);
            }
        });
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
            runAsync("create StringTable " + name, new SQLRunnable() {
                @Override
                public void run() throws SQLException {
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
        public void get(String key, ResultRunnable<String> runWithResult) {

        }

        @Override
        public void set(String key, String value, ResultRunnable<Boolean> runWithResult) {

        }
    }

    @RequiredArgsConstructor
    private class IntTable implements MapTable<String, Integer> {

        private final String name;

        private void create() {
            runAsync("create IntTable " + name, new SQLRunnable() {
                @Override
                public void run() throws SQLException {
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
            runAsync(String.format("get value %s in %s", key, name), new SQLRunnable() {
                @Override
                public void run() throws SQLException {
                    String query = String.format("SELECT `intValue` FROM `%s` WHERE `stringKey` = ?", name);
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, key);
                    Integer result = -1;
                    try {
                        statement.execute();
                        ResultSet set = statement.executeQuery();
                        result = set.getInt(1);
                    } finally {
                        statement.close();
                        runSync(runWithResult, result);
                    }
                }
            });
        }

        @Override
        public void set(final String key, final Integer value, final ResultRunnable<Boolean> runWithResult) {
            runAsync(String.format("set value for %s to %s in %s", key, value, name), new SQLRunnable() {
                @Override
                public void run() throws SQLException {
                    String query = String.format("REPLACE INTO `%s` (`stringKey`, `intValue`) VALUES(?, ?)", name);
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setString(1, key);
                    statement.setInt(2, value);
                    boolean success = false;
                    try {
                        success = statement.executeUpdate() != 0;
                    } finally {
                        statement.close();
                        runSync(runWithResult, success);
                    }
                }
            });
        }
    }
}
