package org.inspirenxe.skills.impl.content.component.apply;

import org.inspirenxe.skills.impl.content.component.filter.EventCompoundFilterQuery;

public abstract class TypedEventApplicator<T> extends EventApplicatorImpl {

    private final Class<T> eventClass;

    protected TypedEventApplicator(Class<T> eventClass) {
        this.eventClass = eventClass;
    }

    @Override
    public void apply(EventCompoundFilterQuery eventData) {
        if (!this.eventClass.isInstance(eventData.getEvent())) {
            throw new IllegalStateException(String.format("Expected event of type '%s'. but got '%s'", this.eventClass, eventData));
        }
        this.applyTyped(eventData, this.eventClass.cast(eventData.getEvent()));
    }

    protected abstract void applyTyped(final EventCompoundFilterQuery eventData, T event);

}
