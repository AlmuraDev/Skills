package org.inspirenxe.skills.impl.content.component.apply.cancel;

import org.inspirenxe.skills.impl.content.component.apply.TypedEventApplicator;
import org.inspirenxe.skills.impl.content.component.filter.EventCompoundFilterQuery;
import org.spongepowered.api.event.Cancellable;

public class EventCancelApplicator extends TypedEventApplicator<Cancellable> {

    private final boolean cancelled;

    public EventCancelApplicator(boolean cancelled) {
        super(Cancellable.class);
        this.cancelled = cancelled;
    }

    @Override
    public void applyTyped(EventCompoundFilterQuery eventData, Cancellable event) {
        event.setCancelled(this.cancelled);
    }
}
