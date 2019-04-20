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

import net.kyori.filter.Filter;
import net.kyori.filter.FilterQuery;
import net.kyori.filter.FilterResponse;
import net.kyori.filter.MultiFilter;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;

public final class MatchFilterResponseToResponseFilter extends MultiFilter {

    public static MatchFilterResponseToResponseFilter matchTo(final FilterResponse response, Filter... filters) {
        return new MatchFilterResponseToResponseFilter(response, Arrays.asList(filters));
    }

    private final FilterResponse response;

    private MatchFilterResponseToResponseFilter(final FilterResponse response, final Iterable<? extends Filter> filters) {
        super(filters);
        this.response = response;
    }

    @Override
    public @NonNull FilterResponse query(@NonNull final FilterQuery query) {
        for (final Filter filter : this.filters) {
            FilterResponse fResponse = filter.query(query);
            if (fResponse != this.response) {
                return FilterResponse.ABSTAIN;
            }
        }
        return this.response;
    }
}
