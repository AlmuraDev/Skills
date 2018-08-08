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
package org.inspirenxe.skills.impl.content.component.query;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import net.kyori.fragment.filter.FilterQuery;
import org.inspirenxe.skills.impl.content.component.filter.block.BlockQuery;
import org.inspirenxe.skills.impl.content.component.filter.block.BlockQueryProducer;
import org.inspirenxe.skills.impl.content.component.filter.cause.CauseQuery;
import org.inspirenxe.skills.impl.content.component.filter.cause.CauseQueryProducer;
import org.inspirenxe.skills.impl.content.component.filter.data.DataQuery;
import org.inspirenxe.skills.impl.content.component.filter.data.DataQueryProducer;
import org.inspirenxe.skills.impl.content.component.filter.experience.LevelQuery;
import org.inspirenxe.skills.impl.content.component.filter.experience.LevelQueryProducer;
import org.inspirenxe.skills.impl.content.component.filter.item.ItemQuery;
import org.inspirenxe.skills.impl.content.component.filter.item.ItemQueryProducer;
import org.inspirenxe.skills.impl.content.component.filter.owner.BlockSnapshotOwnerQueryProducer;
import org.inspirenxe.skills.impl.content.component.filter.owner.OwnerQuery;
import org.spongepowered.api.event.Event;

public final class EventFilterProducerRegistry {

    public static final EventFilterProducerRegistry INSTANCE = new EventFilterProducerRegistry();

    private final Multimap<Class<? extends FilterQuery>, EventFilterQueryProducer<? extends Event, ? extends FilterQuery>> producers =
      HashMultimap.create();

    private <T extends FilterQuery> void registerProducer(final Class<T> filterClass, final EventFilterQueryProducer<? extends Event, T> producer) {
        this.producers.put(filterClass, producer);
    }

    private EventFilterProducerRegistry() {
        this.registerProducer(BlockQuery.class, new BlockQueryProducer());
        this.registerProducer(ItemQuery.class, new ItemQueryProducer());
        this.registerProducer(CauseQuery.class, new CauseQueryProducer());
        this.registerProducer(DataQuery.class, new DataQueryProducer());
        this.registerProducer(OwnerQuery.class, new BlockSnapshotOwnerQueryProducer());
        this.registerProducer(LevelQuery.class, new LevelQueryProducer());
    }

    @SuppressWarnings("unchecked")
    public ImmutableMap<Class<? extends FilterQuery>, FilterQuery> getQueries(final Event event) {
        final ImmutableMap.Builder<Class<? extends FilterQuery>, FilterQuery> mapBuilder = ImmutableMap.builder();

        for (final EventFilterQueryProducer<? extends Event, ? extends FilterQuery> producer: this.producers.values()) {
            if (!producer.getEventType().isAssignableFrom(event.getClass())) {
                continue;
            }
            ((EventFilterQueryProducer<Event, FilterQuery>) producer)
                    .produce(event)
                    .ifPresent(f -> mapBuilder.put(producer.getFilterQueryType(), f));
        }
        return mapBuilder.build();
    }
}
