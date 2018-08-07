package org.inspirenxe.skills.impl.content.type.skill.component.event.flatten;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;

public class ChangeBlockEventPreFlattener implements EventFlattener<ChangeBlockEvent.Pre> {

    @Override
    public Collection<ChangeBlockEvent.Pre> flatten(ChangeBlockEvent.Pre event) {
        ImmutableList.Builder<ChangeBlockEvent.Pre> builder = ImmutableList.builder();
        for (Location<World> location: event.getLocations()) {
            builder.add(SpongeEventFactory.createChangeBlockEventPre(event.getCause(), ImmutableList.of(location)));
        }
        return builder.build();
    }
}
