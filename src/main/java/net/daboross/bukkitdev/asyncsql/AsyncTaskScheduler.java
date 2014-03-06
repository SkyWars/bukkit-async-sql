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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class AsyncTaskScheduler {

    private final ExecutorService executorService;
    private final Logger logger;

    public AsyncTaskScheduler(final Logger logger, final String name) {
        this.logger = logger;
        executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(final Runnable r) {
                return new Thread(r, name);
            }
        });
    }

    public void queueRunnable(Runnable runnable) {
        executorService.execute(runnable);
    }

    public void finishUp() {
        try {
            boolean finished = executorService.awaitTermination(5, TimeUnit.MINUTES);
            if (!finished) {
                logger.warning("Didn't finish executing all SQL tasks as it would have taken more than 5 minutes.");
            }
        } catch (InterruptedException e) {
            logger.warning("InterruptedException finishing up SQL tasks.");
        }
    }
}
