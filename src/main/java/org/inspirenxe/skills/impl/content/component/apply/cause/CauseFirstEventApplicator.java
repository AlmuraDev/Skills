package org.inspirenxe.skills.impl.content.component.apply.cause;

import org.inspirenxe.skills.impl.content.component.apply.TypedEventApplicator;
import org.inspirenxe.skills.impl.content.component.filter.EventCompoundFilterQuery;
import org.spongepowered.api.event.Event;

public abstract class CauseFirstEventApplicator<T extends Event, C> extends TypedEventApplicator<T> {

    private final Class<C> causeClass;

    protected CauseFirstEventApplicator(Class<C> causeClass, Class<T> eventClass) {
        super(eventClass);
        this.causeClass = causeClass;
    }

    @Override
    protected final void applyTyped(EventCompoundFilterQuery eventData, T event) {
        C causeObject = event.getCause().first(this.causeClass).orElseThrow(() -> new IllegalStateException(String.format("Expected event '%s' to have '%s' in its cause!", event, this.causeClass)));
        this.applyWithCause(eventData, event, causeObject);
    }

    protected abstract void applyWithCause(EventCompoundFilterQuery eventData, T event, C causeObject);
}
