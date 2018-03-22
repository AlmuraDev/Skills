package org.inspirenxe.skills.impl;

import net.kyori.violet.AbstractModule;
import org.inspirenxe.skills.impl.network.NetworkModule;
import org.inspirenxe.skills.impl.network.PacketBinder;

public final class ServerModule extends AbstractModule {

    public PacketBinder packet() {
        return PacketBinder.create(this.binder());
    }

    @Override
    protected void configure() {
        this.install(new NetworkModule());
    }
}
