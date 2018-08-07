package org.inspirenxe.skills.impl.content.type.skill.component.event.flatten;

import org.spongepowered.api.event.Event;

import java.util.Collection;

public interface EventFlattener<T extends Event> {

    Collection<T> flatten(T event);

}
