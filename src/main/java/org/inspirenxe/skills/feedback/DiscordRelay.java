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
package org.inspirenxe.skills.feedback;

import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.plugin.PluginContainer;

import java.lang.reflect.Method;

/**
 * Relays messages to Discord through UltimateChat's Discord bridge (its log channel), if UltimateChat is installed and its bridge is
 * up. UltimateChat is only reached by reflection on its plugin instance, so Skills runs without it.
 */
final class DiscordRelay {

    private static final String ULTIMATE_CHAT_ID = "ultimatechat";
    private static final String UCHAT_CLASS = "br.net.fabiozumbi12.UltimateChat.Sponge.UChat";

    private final Logger logger;
    private boolean warned;

    DiscordRelay(final Logger logger) {
        this.logger = logger;
    }

    /**
     * UltimateChat's own static {@code UChat.get()}, or its plugin instance if that class can't be reached from here.
     */
    @SuppressWarnings("removal")
    private static Object uChat(final PluginContainer plugin) throws ReflectiveOperationException {
        try {
            final Class<?> type = Class.forName(DiscordRelay.UCHAT_CLASS, true, DiscordRelay.class.getClassLoader());
            return type.getMethod("get").invoke(null);
        } catch (final ClassNotFoundException e) {
            return plugin.instance();
        }
    }

    void relay(final String icon, final String message) {
        final PluginContainer plugin = Sponge.pluginManager().plugin(DiscordRelay.ULTIMATE_CHAT_ID).orElse(null);
        if (plugin == null) {
            return;
        }
        try {
            final Object uChat = DiscordRelay.uChat(plugin);
            if (uChat == null) {
                return;
            }
            final Method getUCJDA = uChat.getClass().getMethod("getUCJDA");
            final Object jda = getUCJDA.invoke(uChat);
            if (jda == null) {
                return;
            }
            final Object available = jda.getClass().getMethod("JDAAvailable").invoke(jda);
            if (!Boolean.TRUE.equals(available)) {
                return;
            }
            jda.getClass().getMethod("sendRawToDiscord", String.class).invoke(jda, icon + " **" + message + "**");
        } catch (final ReflectiveOperationException | RuntimeException | LinkageError e) {
            // Never let the relay break the rest of the level up feedback
            if (!this.warned) {
                this.warned = true;
                this.logger.warn("Failed to relay a level up to Discord via UltimateChat (further failures are not logged).", e);
            }
        }
    }
}
