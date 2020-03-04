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
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillHolderContainer;
import org.inspirenxe.skills.api.SkillService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.service.ServiceManager;

import java.util.Optional;
import java.util.UUID;

public final class SkillLoader implements Witness {

  private final PluginContainer container;
  private final Scheduler scheduler;
  private final ServiceManager serviceManager;
  private final SkillServiceImpl.Factory factory;

  @Inject
  public SkillLoader(final PluginContainer container, final Scheduler scheduler, final ServiceManager serviceManager,
    final SkillServiceImpl.Factory factory) {
    this.container = container;
    this.scheduler = scheduler;
    this.serviceManager = serviceManager;
    this.factory = factory;
  }

  @Listener(order = Order.AFTER_PRE)
  public void onGameConstruction(final GameConstructionEvent event) {
    this.serviceManager.setProvider(this.container, SkillService.class, this.factory.create());
  }

  @Listener(order = Order.PRE)
  public void onLoadWorld(final LoadWorldEvent event) {
    final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

    service.createContainer(event.getTargetWorld().getUniqueId(), event.getTargetWorld().getName());
  }

  @Listener(order = Order.PRE)
  public void onUnloadWorld(final UnloadWorldEvent event) {
    final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

    service.removeContainer(event.getTargetWorld().getUniqueId()).ifPresent(SkillHolderContainer::save);
  }

  @Listener(order = Order.LAST)
  public void onClientConnectionJoinByPlayer(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
    final UUID containerId = player.getWorld().getUniqueId();
    final UUID holderId = player.getUniqueId();

    final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

    this.getContainerOrParent(service, containerId).ifPresent(container -> {
      final SkillHolder holder = container.createHolder(holderId, player.getName());

      this.scheduler
        .createTaskBuilder()
        .async()
        .execute(() -> holder.getSkills().values().forEach(Skill::load))
        .submit(this.container);
    });
  }

  @Listener(order = Order.LAST)
  public void onMoveEntityTeleport(final MoveEntityEvent.Teleport event, @Getter("getTargetEntity") final Player player) {
    final UUID fromContainerId = event.getFromTransform().getExtent().getUniqueId();
    final UUID toContainerId = event.getToTransform().getExtent().getUniqueId();
    final UUID holderId = player.getUniqueId();
    final String holderName = player.getName();

    if (fromContainerId.equals(toContainerId)) {
      return;
    }

    this.handleContainerChange(fromContainerId, toContainerId, holderId, holderName);
  }

  @Listener(order = Order.LAST)
  public void onRespawnPlayer(final RespawnPlayerEvent event, @Getter("getTargetEntity") final Player player) {
    final UUID fromContainerId = event.getFromTransform().getExtent().getUniqueId();
    final UUID toContainerId = event.getToTransform().getExtent().getUniqueId();
    final UUID holderId = player.getUniqueId();
    final String holderName = player.getName();

    if (fromContainerId.equals(toContainerId)) {
      return;
    }

    this.handleContainerChange(fromContainerId, toContainerId, holderId, holderName);
  }

  @Listener(order = Order.PRE)
  public void onClientConnectionDisconnectByPlayer(final ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") final Player player) {
    final UUID containerId = player.getWorld().getUniqueId();
    final UUID holderId = player.getUniqueId();

    final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

    try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
      this.getContainerOrParent(service, containerId).ifPresent(container -> container.removeHolder(holderId).ifPresent(SkillHolder::save));
    }
  }

  private Optional<SkillHolderContainer> getContainerOrParent(final SkillService service, final UUID containerId) {
    SkillHolderContainer container = service.getContainer(containerId).orElse(null);

    if (container != null) {
      container = service.getParentContainer(container).orElse(null);
    }

    return Optional.ofNullable(container);
  }

  private void handleContainerChange(final UUID fromContainerId, final UUID toContainerId, final UUID holderId, final String holderName) {
    final SkillService service = this.serviceManager.provideUnchecked(SkillService.class);

    final SkillHolderContainer fromContainer = service.getContainer(fromContainerId).orElse(null);

    boolean remove = true;

    if (fromContainer == null) {
      remove = false;
    }

    final SkillHolderContainer toContainer = this.getContainerOrParent(service, toContainerId).orElse(null);
    if (fromContainer == null || fromContainer == toContainer) {
      remove = false;
    }

    final SkillHolder holder;
    if (toContainer != null) {
      holder = toContainer.createHolder(holderId, holderName);
    } else {
      holder = null;
    }

    final boolean result = remove;
    
    this.scheduler
      .createTaskBuilder()
      .async()
      .execute(() -> {
        if (result) {
          fromContainer.removeHolder(holderId).ifPresent(SkillHolder::save);
        }

        if (holder != null) {
          holder.getSkills().values().forEach(Skill::load);
        }
      })
      .submit(this.container);
  }
}
