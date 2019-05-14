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
package org.inspirenxe.skills.api.skill.builtin.filter.data;

import net.kyori.filter.FilterQuery;
import net.kyori.filter.TypedFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.inspirenxe.skills.api.skill.builtin.query.EventQuery;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.event.cause.EventContextKey;

import java.util.Collection;

public class ValueFilter<T extends DataHolder, V, U extends BaseValue<V>> implements TypedFilter.Strong<EventQuery> {

    private final EventContextKey<T> processingKey;
    private final Key<U> key;
    private final Collection<V> values;

    ValueFilter(final EventContextKey<T> processingKey, final Key<U> key, final Collection<V> values) {
        this.processingKey = processingKey;
        this.key = key;
        this.values = values;
    }

    @Override
    public boolean queryResponse(@NonNull final EventQuery query) {
        final V current = query.getContext().get(this.processingKey).orElse(null).get(this.key).orElse(null);
        if (current == null) {
            query.denied(this);
            return false;
        }

        boolean matched = false;
        for (V v : this.values) {
            if (v.equals(current)) {
                matched = true;
                break;
            }
        }

        if (!matched) {
            query.denied(this);
        }

        return matched;
    }

    @Override
    public boolean queryable(@NonNull final FilterQuery query) {
        if (!(query instanceof EventQuery)) {
            return false;
        }
        return ((EventQuery) query).getContext().containsKey(this.processingKey);
    }
}
