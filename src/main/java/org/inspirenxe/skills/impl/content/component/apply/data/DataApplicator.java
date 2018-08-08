package org.inspirenxe.skills.impl.content.component.apply.data;

import org.inspirenxe.skills.impl.content.component.apply.MathOperationType;
import org.inspirenxe.skills.impl.content.component.apply.cause.CauseFirstEventApplicator;
import org.inspirenxe.skills.impl.content.component.filter.EventCompoundFilterQuery;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.event.Event;

import java.math.BigDecimal;

import javax.annotation.Nullable;

public class DataApplicator extends CauseFirstEventApplicator<Event, DataHolder> {

    private KeyValue keyValue;
    @Nullable private MathOperationType operation;

    protected DataApplicator(KeyValue keyValue, @Nullable MathOperationType operationType) {
        super(DataHolder.class, Event.class);
        this.keyValue = keyValue;
        this.operation = operation;
    }

    @Override
    protected void applyWithCause(EventCompoundFilterQuery eventData, Event event, DataHolder causeObject) {
        Object newValueObj = this.keyValue.getValue();
        if (this.operation != null) {
            BigDecimal oldValue = new BigDecimal(((Number) causeObject.getValue((Key) this.keyValue.getKey()).get()).toString());
            BigDecimal newValue = new BigDecimal(((Number) newValueObj).toString());
            newValueObj = this.operation.apply(oldValue, newValue);
        }
        causeObject.offer((Key) this.keyValue.getKey(), newValueObj);
    }
}
