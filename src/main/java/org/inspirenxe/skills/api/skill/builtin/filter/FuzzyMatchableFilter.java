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
package org.inspirenxe.skills.api.skill.builtin.filter;

import net.kyori.filter.FilterQuery;
import net.kyori.filter.TypedFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.inspirenxe.skills.api.skill.builtin.FuzzyMatchable;
import org.inspirenxe.skills.api.skill.builtin.query.TypedEventQuery;

import java.util.Collection;

public class FuzzyMatchableFilter<O, T extends FuzzyMatchable<O>> implements TypedFilter.Strong<TypedEventQuery<O>> {

    private final Collection<T> value;

    public FuzzyMatchableFilter(final Collection<T> value) {
        this.value = value;
    }

    public Collection<T> getValue() {
        return this.value;
    }

    @Override
    public final boolean queryResponse(@NonNull final TypedEventQuery<O> query) {
        final boolean matched = this.value.stream().anyMatch(v -> v.matches(query.getValue()));
        if (!matched) {
            query.denied(this);
            return false;
        }

        return true;
    }

    @Override
    public final boolean queryable(@NonNull final FilterQuery query) {
        return query instanceof TypedEventQuery;
    }
}
