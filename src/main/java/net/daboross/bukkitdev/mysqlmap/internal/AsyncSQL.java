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
package net.daboross.bukkitdev.mysqlmap.internal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.daboross.bukkitdev.mysqlmap.SQLConnectionInfo;
import net.daboross.bukkitdev.mysqlmap.api.ResultRunnable;
import net.daboross.bukkitdev.mysqlmap.api.SQLConnection;
import net.daboross.bukkitdev.mysqlmap.api.SQLRunnable;
import org.bukkit.plugin.Plugin;

public class AsyncSQL implements SQLConnection {

    private final AsyncTaskScheduler taskScheduler;
    private final Logger logger;
    private final Plugin plugin;
    private final SQLConnectionInfo connectionInfo;
    private Connection connection;

    public AsyncSQL(Plugin plugin, Logger logger, SQLConnectionInfo connectionInfo) throws SQLException {
        this.logger = logger;
        this.plugin = plugin;
        this.connectionInfo = connectionInfo;
        this.taskScheduler = new AsyncTaskScheduler(plugin, logger, "SQL Task Thread for " + connectionInfo.getUrl());
        this.taskScheduler.start();
        connectToSQL();
    }

    public void connectToSQL() throws SQLException {
        try {
            connection = connectionInfo.createConnection();
        } catch (SQLException ex) {
            logger.log(Level.WARNING, "Failed to create connection to `" + connectionInfo.getUrl() + "`", ex);
            throw ex;
        }
    }

    @Override
    public void run(final String taskName, final SQLRunnable runnable) {
        taskScheduler.queueRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
                        runnable.run(connection);
                    } catch (SQLException ex) {
                        try {
                            logger.log(Level.INFO, "Failed to " + taskName + ": " + ex.getMessage() + ". Reconnecting and retrying.");
                            connection.close();
                            connectToSQL();
                            runnable.run(connection);
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

    @Override
    public <T> void run(final String taskName, final ResultSQLRunnable<T> runnable, final ResultRunnable<T> runWithResult) {
        taskScheduler.queueRunnable(new Runnable() {
            @Override
            public void run() {
                ResultHolder<T> result = new ResultHolder<T>();
                try {
                    try {
                        runnable.run(connection, result);
                    } catch (SQLException ex) {
                        try {
                            logger.log(Level.INFO, "Failed to " + taskName + ": " + ex.getMessage() + ". Reconnecting and retrying.");
                            connection.close();
                            connectToSQL();
                            runnable.run(connection, result);
                        } catch (SQLException ex2) {
                            logger.log(Level.WARNING, "Failed to " + taskName + ", not retrying:", ex2);
                        }
                    }
                } catch (RuntimeException ex) {
                    throw new RuntimeException("Exception " + taskName + ":", ex);
                } finally {
                    runSync(runWithResult, result.get());
                }
            }
        });
    }

    @Override
    public <T> void runSync(final ResultRunnable<T> runWithResult, final T result) {
        if (runWithResult != null) {
            plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    runWithResult.runWithResult(result);
                }
            });
        }
    }

    @Override
    public void finishUp() {
        taskScheduler.finishUp();
    }
}
