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

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class AsyncTaskScheduler implements Runnable {

    private final Queue<Runnable> queue = new LinkedList<Runnable>();
    @NonNull
    private final Plugin plugin;
    @NonNull
    private final Logger logger;
    private final String name;

    public void start() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this);
    }

    public void queueRunnable(Runnable toRun) {
        synchronized (queue) {
            queue.add(toRun);
            queue.notify();
        }
    }

    @Override
    public void run() {
        if (name != null)
            Thread.currentThread().setName(name);
        while (true) {
            Runnable next;
            synchronized (queue) {
                next = queue.poll();
                if (next == null) {
                    try {
                        queue.wait();
                    } catch (InterruptedException ex) {
                        logger.log(Level.SEVERE, String.format("InterruptedException in the %s thread.", name == null ? "AsyncTaskScheduler" : name), ex);
                    }
                }
            }
            if (next != null) {
                next.run();
            }
        }
    }
}
