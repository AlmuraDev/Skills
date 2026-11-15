/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE
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
package org.inspirenxe.skills;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;

import java.time.Duration;

/**
 * Schedules work on the server thread.
 */
public final class Tasks {

    private static PluginContainer plugin;

    private Tasks() {
    }

    static void init(final PluginContainer plugin) {
        Tasks.plugin = plugin;
    }

    public static PluginContainer plugin() {
        return Tasks.plugin;
    }

    /**
     * Runs the task on the server thread, on the next tick.
     */
    public static void sync(final Runnable runnable) {
        Sponge.server().scheduler().submit(Task.builder().plugin(Tasks.plugin).execute(runnable).build());
    }

    public static void later(final long ticks, final Runnable runnable) {
        Sponge.server().scheduler().submit(Task.builder().plugin(Tasks.plugin).delay(Ticks.of(ticks)).execute(runnable).build());
    }

    public static ScheduledTask repeat(final String name, final Duration interval, final Runnable runnable) {
        return Sponge.server().scheduler().submit(Task.builder()
                .plugin(Tasks.plugin)
                .delay(interval)
                .interval(interval)
                .execute(runnable)
                .build(), name);
    }
}
