package org.inspirenxe.skills.impl.content.type.skill.component.event.flatten;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.event.Event;

import java.util.Collection;

public class NoOpEventFlattener<T extends Event> implements EventFlattener<T> {

    @Override
    public Collection<T> flatten(T event) {
        return ImmutableList.of(event);
    }
}
