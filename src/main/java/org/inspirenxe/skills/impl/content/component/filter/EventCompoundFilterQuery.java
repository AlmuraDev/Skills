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
package org.inspirenxe.skills.impl.content.component.filter;

import com.google.common.collect.ImmutableMap;
import net.kyori.fragment.filter.FilterQuery;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.content.component.query.EventFilterProducerRegistry;
import org.spongepowered.api.event.Event;

import java.util.Collection;
import java.util.Optional;

public class EventCompoundFilterQuery implements FilterQuery {

    private final Event event;
    private final ImmutableMap<Class<? extends FilterQuery>, FilterQuery> filterQueries;
    private final SkillType skillType;

    public EventCompoundFilterQuery(final Event event, final SkillType skillType) {
        this.event = event;
        this.filterQueries = EventFilterProducerRegistry.INSTANCE.getQueries(event);
        this.skillType = skillType;
    }

    public <T extends FilterQuery> Optional<T> getQuery(final Class<T> filterClass) {
        return Optional.ofNullable(filterClass.cast(this.filterQueries.get(filterClass)));
    }

    public Event getEvent() {
        return this.event;
    }

    public SkillType getSkillType() {
        return this.skillType;
    }

    public Collection<FilterQuery> getAll() {
        return this.filterQueries.values();
    }

}
