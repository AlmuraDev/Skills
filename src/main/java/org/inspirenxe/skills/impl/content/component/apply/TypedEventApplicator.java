package org.inspirenxe.skills.impl.content.component.apply;

import org.spongepowered.api.event.Event;

public abstract class TypedEventApplicator<E extends Event> implements EventApplicator<E> {

    private final Class<?> eventClass;

    protected TypedEventApplicator(Class<?> eventClass) {
        this.eventClass = eventClass;
    }

    @Override
    public boolean accepts(Event trigger) {
        return this.eventClass.isInstance(trigger);
    }

}
