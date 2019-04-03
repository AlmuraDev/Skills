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
package org.inspirenxe.skills.api.skill.builtin.filter.deny;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.toolbox.util.math.IntRange;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.builtin.filter.FilterResult;
import org.inspirenxe.skills.api.skill.builtin.query.Query;
import org.spongepowered.api.event.cause.Cause;

public final class DenyFilters {

    public static <T, Q extends Query<T>> DenyFilter<T, Q> level(final Q query, final int level) {
        checkNotNull(query);
        checkArgument(level >= 0);

        return new DenyFilter<T, Q>() {
            @Override
            public Q getQuery() {
                return query;
            }

            @Override
            public FilterResult test(final Cause cause, final Skill skill, final T value) {
                if (!query.matches(value)) {
                    return FilterResult.SKIP;
                }

                if (level > skill.getCurrentLevel()) {
                    return FilterResult.FAIL;
                }

                return FilterResult.SUCCESS;
            }
        };
    }

    public static <T, Q extends Query<T>> DenyFilter<T, Q> levelRange(final Q query, final IntRange range) {
        checkNotNull(query);
        checkNotNull(range);
        checkArgument(range.min() >= 0);

        return new DenyFilter<T, Q>() {
            @Override
            public Q getQuery() {
                return query;
            }

            @Override
            public FilterResult test(final Cause cause, final Skill skill, final T value) {
                if (!query.matches(value)) {
                    return FilterResult.SKIP;
                }

                if (skill.getCurrentLevel() < range.min() || skill.getCurrentLevel() > range.max()) {
                    return FilterResult.FAIL;
                }

                return FilterResult.SUCCESS;
            }
        };
    }

    private DenyFilters() {
    }
}
