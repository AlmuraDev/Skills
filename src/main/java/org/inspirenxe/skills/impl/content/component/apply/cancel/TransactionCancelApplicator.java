package org.inspirenxe.skills.impl.content.component.apply.cancel;

import org.inspirenxe.skills.impl.content.component.apply.TypedEventApplicator;
import org.inspirenxe.skills.impl.content.component.filter.EventCompoundFilterQuery;
import org.spongepowered.api.event.block.ChangeBlockEvent;

public class TransactionCancelApplicator extends TypedEventApplicator<ChangeBlockEvent> {

    private final boolean cancelled;

    protected TransactionCancelApplicator(boolean cancelled) {
        super(ChangeBlockEvent.class);
        this.cancelled = cancelled;
    }

    @Override
    protected void applyTyped(EventCompoundFilterQuery eventData, ChangeBlockEvent event) {
        event.getTransactions().get(0).setValid(!cancelled);
    }
}
