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

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.spongepowered.api.Platform;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.Message;
import org.spongepowered.api.network.MessageHandler;

import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class PacketBinder {

    private final Multibinder<EntryImpl<? extends Message>> binder;

    public static PacketBinder create(final Binder binder) {
        return new PacketBinder(binder);
    }

    private PacketBinder(@Nonnull final Binder binder) {
        this.binder = Multibinder.newSetBinder(binder, new TypeLiteral<EntryImpl<? extends Message>>() {});
    }

    /**
     * Create a binding entry for the specified packet.
     *
     * <p>Bound packets will be installed into the network channel.</p>
     *
     * @param packet the packet class
     * @param consumer the binder consumer
     * @param <M> the packet type
     * @return this binder
     */
    public <M extends Message> PacketBinder bind(final Class<M> packet, final Consumer<Entry<M>> consumer) {
        final EntryImpl<M> entry = new EntryImpl<>(packet);
        consumer.accept(entry);
        this.binder.addBinding().toInstance(entry);
        return this;
    }

    /**
     * A binding entry.
     *
     * @param <M> the packet type
     */
    public interface Entry<M extends Message> {

        /**
         * Sets the channel id.
         *
         * @param channel the channel id
         */
        void channel(final int channel);

        /**
         * Sets the packet handler.
         *
         * @param handler the handler
         * @param side the side
         */
        void handler(final Class<? extends MessageHandler<M>> handler, final Platform.Type side);
    }

    public static final class EntryImpl<M extends Message> implements Entry<M> {

        private final Class<M> packet;
        @Nullable private Integer channel;
        @Nullable private Platform.Type side;
        @Nullable private Class<? extends MessageHandler<M>> handler;

        EntryImpl(final Class<M> packet) {
            this.packet = packet;
        }

        public void register(final ChannelBinding.IndexedMessageChannel channel, final Platform.Type side, final Injector injector) {
            if (this.channel != null) {
                channel.registerMessage(this.packet, this.channel);
            }

            checkState(this.side != null, "side not provided");
            checkState(this.handler != null, "handler not provided");
            if (this.side == side) {
                channel.addHandler(this.packet, this.side, injector.getInstance(this.handler));
            }
        }

        @Override
        public void channel(final int channel) {
            this.channel = channel;
        }

        @Override
        public void handler(final Class<? extends MessageHandler<M>> handler, final Platform.Type side) {
            this.handler = handler;
            this.side = side;
        }
    }
}
