package org.inspirenxe.skills.impl.content.component.apply.cancel;

import org.inspirenxe.skills.impl.content.component.apply.TypedEventApplicator;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;

public class EventCancelApplicator extends TypedEventApplicator<Event> {

    private final boolean cancelled;

    public EventCancelApplicator(boolean cancelled) {
        super(Cancellable.class);
        this.cancelled = cancelled;
    }

    @Override
    public void applyInternal(Event trigger) {
        ((Cancellable) trigger).setCancelled(this.cancelled);
    }
}
