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