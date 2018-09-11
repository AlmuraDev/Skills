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

import com.almuradev.toolbox.inject.event.Witness;
import com.google.inject.Inject;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.ServiceManager;

import java.util.Set;
import java.util.UUID;

public final class SkillLoader implements Witness {

  private final PluginContainer container;
  private final ServiceManager serviceManager;
  private final SkillServiceImpl.Factory serviceFactory;

  @Inject
  public SkillLoader(final PluginContainer container, final ServiceManager serviceManager, final SkillServiceImpl.Factory serviceFactory) {
    this.container = container;
    this.serviceManager = serviceManager;
    this.serviceFactory = serviceFactory;
  }

  @Listener(order = Order.AFTER_PRE)
  public void onGameConstruction(final GameConstructionEvent event) {
    this.serviceManager.setProvider(this.container, SkillService.class, this.serviceFactory.create());
  }

  @Listener
  public void onUnloadWorld(final UnloadWorldEvent event) {
    final SkillService skillService = this.serviceManager.provideUnchecked(SkillService.class);
    skillService.saveContainer(event.getTargetWorld().getUniqueId(), false);
    skillService.removeContainer(event.getTargetWorld().getUniqueId());
  }

  @Listener(order = Order.LAST)
  public void onClientConnectionJoinByPlayer(final ClientConnectionEvent.Join event, @Root final Player player) {
    final UUID container = Sponge.getServer().getDefaultWorld().get().getUniqueId();
    final UUID holder = player.getUniqueId();

    final SkillService skillService = this.serviceManager.provideUnchecked(SkillService.class);

    skillService.loadHolder(container, holder, true);
  }

  @Listener
  public void onClientConnectionDisconnectByPlayer(final ClientConnectionEvent.Disconnect event, @Root final Player player) {
    final UUID container = Sponge.getServer().getDefaultWorld().get().getUniqueId();
    final UUID holder = player.getUniqueId();

    final SkillService skillService = this.serviceManager.provideUnchecked(SkillService.class);

    // Save skills for the holder in the old container and remove them
    final Set<SkillHolder> holders = skillService.getHoldersInContainer(container);
    if (holders.isEmpty()) {
      return;
    }

    final SkillHolder skillHolder = holders
      .stream()
      .filter((h) -> h.getHolderUniqueId().equals(holder))
      .findFirst()
      .orElse(null);

    if (skillHolder == null) {
      return;
    }

    if (holders.size() == 1) {
      skillService.saveContainer(container, false);
      skillService.removeContainer(container);
    } else {
      holders.remove(skillHolder);
    }
  }
}
