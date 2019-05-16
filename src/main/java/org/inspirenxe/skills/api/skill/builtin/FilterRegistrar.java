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

import net.kyori.filter.Filter;
import org.inspirenxe.skills.api.skill.builtin.filter.trigger.TriggerFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FilterRegistrar {

    public static FilterRegistrar.Builder registrar() {
        return new Builder();
    }

    private final Map<RegistrarType, List<Filter>> filters;
    private final Map<TriggerRegistrarType, List<TriggerFilter>> triggers;

    private FilterRegistrar(final Builder builder) {
        this.filters = builder.filters;
        this.triggers = builder.triggers;
    }

    public List<Filter> getFilters(final RegistrarType type) {
        return this.filters.computeIfAbsent(type, k -> new ArrayList<>());
    }

    public List<TriggerFilter> getTriggers(final TriggerRegistrarType type) {
        return this.triggers.computeIfAbsent(type, k -> new ArrayList<>());
    }

    public static final class Builder {
        final Map<RegistrarType, List<Filter>> filters = new HashMap<>();
        final Map<TriggerRegistrarType, List<TriggerFilter>> triggers = new HashMap<>();

        public Builder addFilter(final RegistrarType type, final Filter... filters) {
            Collections.addAll(this.filters.computeIfAbsent(type, k -> new ArrayList<>()), filters);
            return this;
        }

        public Builder addTrigger(final TriggerRegistrarType type, final TriggerFilter... triggers) {
            Collections.addAll(this.triggers.computeIfAbsent(type, k -> new ArrayList<>()), triggers);
            return this;
        }

        public FilterRegistrar build() {
            return new FilterRegistrar(this);
        }
    }
}
