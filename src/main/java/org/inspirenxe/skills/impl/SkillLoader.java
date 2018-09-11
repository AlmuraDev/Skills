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
