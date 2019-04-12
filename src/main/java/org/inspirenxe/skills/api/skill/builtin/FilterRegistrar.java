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
package org.inspirenxe.skills.api.skill.builtin;

import static com.google.common.base.Preconditions.checkState;

import net.kyori.filter.Filter;
import org.inspirenxe.skills.api.skill.builtin.filter.applicator.TriggerFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FilterRegistrar {

    public static FilterRegistrar.Builder registrar() {
        return new Builder();
    }

    private final Filter cancelEvent;
    private final Filter cancelTransaction;
    private final List<TriggerFilter> eventTriggers, transactionTriggers;

    private FilterRegistrar(final Builder builder) {
        this.cancelEvent = builder.cancelEvent;
        this.cancelTransaction = builder.cancelTransaction;
        this.eventTriggers = builder.eventTriggers;
        this.transactionTriggers = builder.transactionTriggers;
    }

    public Filter getCancelEvent() {
        return this.cancelEvent;
    }

    public Filter getCancelTransaction() {
        return this.cancelTransaction;
    }

    public List<TriggerFilter> getEventTriggers() {
        return this.eventTriggers;
    }

    public List<TriggerFilter> getTransactionTriggers() {
        return this.transactionTriggers;
    }

    public static final class Builder {
        private Filter cancelEvent;
        private Filter cancelTransaction;
        private List<TriggerFilter> eventTriggers = new ArrayList<>(), transactionTriggers = new ArrayList<>();

        public Builder cancelEvent(final Filter cancelEvent) {
            this.cancelEvent = cancelEvent;
            return this;
        }

        public Builder cancelTransaction(final Filter cancelTransaction) {
            this.cancelTransaction = cancelTransaction;
            return this;
        }

        public Builder eventTrigger(final TriggerFilter... triggers) {
            this.eventTriggers.addAll(Arrays.asList(triggers));
            return this;
        }

        public Builder transactionTrigger(final TriggerFilter... triggers) {
            this.transactionTriggers.addAll(Arrays.asList(triggers));
            return this;
        }

        public FilterRegistrar build() {
            checkState(this.cancelEvent != null || this.cancelTransaction != null || this.eventTriggers.isEmpty() || this.transactionTriggers.isEmpty(),
                "A FilterRegistrar must do something!");

            return new FilterRegistrar(this);
        }
    }
}
