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
package org.inspirenxe.skills.impl.content.type.function.level;

import com.almuradev.droplet.content.loader.ChildContentLoaderImpl;
import com.almuradev.droplet.registry.Registry;
import com.almuradev.toolbox.inject.event.Witness;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.inspirenxe.skills.api.function.level.LevelFunction;
import org.inspirenxe.skills.impl.content.type.function.ContentFunction;
import org.inspirenxe.skills.impl.registry.module.LevelFunctionRegistryModule;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;

@Singleton
public final class LevelFunctionRootLoader extends ChildContentLoaderImpl<ContentFunction.Child> implements Witness {

  private final Registry<LevelFunction> registry;

  @Inject
  public LevelFunctionRootLoader(final Registry<LevelFunction> registry) {
    this.registry = registry;
  }

  @Listener(order = Order.PRE)
  public void onGameStartingServer(GameStartingServerEvent event) {
    this.foundContent().entries().forEach(entry -> this.registry.put(entry.key(), entry.result(LevelFunction.class)));
  }
}
