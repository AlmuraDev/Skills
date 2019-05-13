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
package org.inspirenxe.skills.api.skill.builtin.filter.applicator;

import static com.google.common.base.Preconditions.checkNotNull;

import net.kyori.filter.Filter;
import net.kyori.filter.FilterQuery;
import net.kyori.filter.FilterResponse;
import net.kyori.filter.MultiFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.inspirenxe.skills.api.skill.builtin.applicator.Applicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TriggerFilter extends MultiFilter {

    private final Iterable<ApplicatorEntry> applicatorEntries;
    private final Iterable<Applicator> elseApplicators;

    private TriggerFilter(@NonNull final Iterable<? extends Filter> filters, @NonNull final Iterable<ApplicatorEntry> applicatorEntries,
        final Iterable<Applicator> elseApplicators) {
        super(filters);
        this.applicatorEntries = applicatorEntries;
        this.elseApplicators = elseApplicators;
    }

    public static Builder triggerIf() {
        return new Builder();
    }

    @Override
    public @NonNull FilterResponse query(@NonNull final FilterQuery query) {
        FilterResponse response = FilterResponse.ALLOW;

        for (Filter filter : this.filters) {
            FilterResponse r = filter.query(query);
            if (r == FilterResponse.DENY) {
                response = FilterResponse.DENY;
                break;
            }
        }

        return response;
    }

    public Iterable<ApplicatorEntry> getMatchedApplicators(@NonNull final FilterQuery query) {
        final List<ApplicatorEntry> matched = new ArrayList<>();

        for (final ApplicatorEntry entry : this.applicatorEntries) {
            boolean passed = true;

            for (final Filter filter : entry.getFilters()) {
                if (filter.query(query) != FilterResponse.ALLOW) {
                    passed = false;
                    break;
                }
            }

            if (passed) {
                matched.add(entry);
            }
        }

        return matched;
    }

    @Nullable
    public Iterable<Applicator> getElseApplicators() {
        return this.elseApplicators;
    }

    public static final class Builder {

        private Iterable<Filter> filters;
        private Iterable<ApplicatorEntry> applicatorFilters;
        private Iterable<Applicator> elseApplicators;

        public TriggerFilter.Builder all(final Filter... filters) {
            this.filters = Arrays.asList(filters);
            return this;
        }

        public TriggerFilter.Builder any(final Filter... filters) {
            this.filters = Arrays.asList(filters);
            return this;
        }

        public TriggerFilter.Builder then(final ApplicatorEntry... filters) {
            this.applicatorFilters = Arrays.asList(filters);
            return this;
        }

        public TriggerFilter.Builder elseApply(final Applicator... applicators) {
            this.elseApplicators = Arrays.asList(applicators);
            return this;
        }

        public TriggerFilter build() {
            checkNotNull(this.filters);
            checkNotNull(this.applicatorFilters);

            return new TriggerFilter(this.filters, this.applicatorFilters, this.elseApplicators);
        }
    }
}
