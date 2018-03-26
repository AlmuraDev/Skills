/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE <https://github.com/InspireNXE/>
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
package org.inspirenxe.skills.impl.network;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import org.inspirenxe.skills.impl.Constants;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelId;
import org.spongepowered.api.network.Message;

import java.util.Set;

import javax.inject.Inject;

public final class NetworkModule extends AbstractModule {

    @Override
    protected void configure() {
        this.requestInjection(this);
    }

    @Inject
    private void configure(
            final Platform platform,
            final Injector injector,
            @ChannelId(Constants.Plugin.NETWORK_CHANNEL) final ChannelBinding.IndexedMessageChannel channel,
            final Set<PacketBinder.EntryImpl<? extends Message>> entries
    ) {
        for (final PacketBinder.EntryImpl<? extends Message> entry : entries) {
            entry.register(channel, platform.getType(), injector);
        }
    }
}