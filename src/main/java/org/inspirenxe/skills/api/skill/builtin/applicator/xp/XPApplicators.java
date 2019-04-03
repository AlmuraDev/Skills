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
package org.inspirenxe.skills.api.skill.builtin.applicator.xp;

import com.almuradev.toolbox.util.math.DoubleRange;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.builtin.query.Query;
import org.spongepowered.api.event.cause.Cause;

import java.util.Random;

public final class XPApplicators {

    private static final Random random = new Random();

    public static <T, Q extends Query<T>> XPApplicator<T, Q> xp(final Q query, final double amount) {
        return new XPApplicator<T, Q>() {
            @Override
            public Q getQuery() {
                return query;
            }

            @Override
            public void apply(final Cause cause, final Skill skill, final T value) {
                if (!this.getQuery().matches(value)) {
                    return;
                }

                skill.addExperience(amount);
            }
        };
    }

    public static <T, Q extends Query<T>> XPApplicator<T, Q> variableXP(final Q query, final DoubleRange range) {
        return new XPApplicator<T, Q>() {
            @Override
            public Q getQuery() {
                return query;
            }

            @Override
            public void apply(final Cause cause, final Skill skill, final T value) {
                if (!this.getQuery().matches(value)) {
                    return;
                }

                skill.addExperience(range.random(random));
            }
        };
    }

    private XPApplicators() {
    }
}
