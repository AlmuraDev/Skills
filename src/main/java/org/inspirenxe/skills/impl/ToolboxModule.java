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
package org.inspirenxe.skills.impl;

import com.almuradev.toolbox.inject.ToolboxBinder;
import com.almuradev.toolbox.inject.command.CommandInstaller;
import com.almuradev.toolbox.inject.event.WitnessModule;
import com.almuradev.toolbox.inject.network.packet.indexed.ForIndexedPacketBinder;
import com.almuradev.toolbox.inject.registry.RegistryInstaller;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import net.kyori.violet.AbstractModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.plugin.PluginContainer;

import javax.inject.Inject;

public final class ToolboxModule extends AbstractModule implements ToolboxBinder {

  @Override
  protected void configure() {
    // Setup event injection
    this.install(new WitnessModule());

    // Setup channel injection
    this.bind(ChannelBinding.IndexedMessageChannel.class).annotatedWith(ForIndexedPacketBinder.class)
        .toProvider(new Provider<ChannelBinding.IndexedMessageChannel>() {
          @Inject private PluginContainer container;

          @Override
          public ChannelBinding.IndexedMessageChannel get() {
            return Sponge.getChannelRegistrar().getOrCreate(this.container, "SKS");
          }
        }).in(Scopes.SINGLETON);

    // Setup installers
    this.facet()
        .add(CommandInstaller.class)
        .add(RegistryInstaller.class);

  }
}
