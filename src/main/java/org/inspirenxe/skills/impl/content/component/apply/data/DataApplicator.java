/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.inspirenxe.skills.impl.content.component.apply.data;

import org.inspirenxe.skills.impl.content.component.apply.MathOperationType;
import org.inspirenxe.skills.impl.content.component.apply.cause.CauseFirstEventApplicator;
import org.inspirenxe.skills.impl.content.component.filter.EventCompoundFilterQuery;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.event.Event;

import java.math.BigDecimal;

import javax.annotation.Nullable;

public final class DataApplicator extends CauseFirstEventApplicator<Event, DataHolder> {

    private final KeyValue keyValue;
    @Nullable private final MathOperationType operationType;

    DataApplicator(final KeyValue keyValue, @Nullable final MathOperationType operationType) {
        super(DataHolder.class, Event.class);
        this.keyValue = keyValue;
        this.operationType = operationType;
    }

    @Override
    protected void applyWithCause(final EventCompoundFilterQuery eventData, final Event event, final DataHolder causeObject) {
        Object newValueObj = this.keyValue.getValue();
        // TODO newValueObj can be null, what do we do in that case?
        if (this.operationType != null) {
            final BigDecimal oldValue = new BigDecimal(causeObject.getValue((Key) this.keyValue.getKey()).get().toString());
            final BigDecimal newValue = new BigDecimal(newValueObj.toString());
            newValueObj = this.operationType.apply(oldValue, newValue);
        }
        causeObject.offer((Key) this.keyValue.getKey(), newValueObj);
    }
}
