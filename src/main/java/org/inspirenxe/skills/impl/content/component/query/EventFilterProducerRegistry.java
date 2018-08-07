package org.inspirenxe.skills.impl.content.component.query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import net.kyori.fragment.filter.FilterQuery;
import org.inspirenxe.skills.impl.content.component.filter.CompoundFilterQuery;
import org.inspirenxe.skills.impl.content.component.filter.block.BlockQuery;
import org.inspirenxe.skills.impl.content.component.filter.block.BlockQueryProducer;
import org.inspirenxe.skills.impl.content.component.filter.cause.CauseQuery;
import org.inspirenxe.skills.impl.content.component.filter.cause.CauseQueryProducer;
import org.spongepowered.api.event.Event;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EventFilterProducerRegistry {

    public static EventFilterProducerRegistry INSTANCE = new EventFilterProducerRegistry();

    private Map<Class<? extends FilterQuery>, EventFilterQueryProducer<? extends Event, ? extends FilterQuery>> producers = new HashMap<>();

    private <T extends FilterQuery> void registerProducer(Class<T> filterClass, EventFilterQueryProducer<? extends Event, T> producer) {
        this.producers.put(filterClass, producer);
    }

    private EventFilterProducerRegistry() {
        this.registerProducer(BlockQuery.class, new BlockQueryProducer());
        this.registerProducer(CauseQuery.class, new CauseQueryProducer());
    }

    @SuppressWarnings("unchecked")
    public CompoundFilterQuery getQueries(Event event) {
        ImmutableMap.Builder<Class<? extends FilterQuery>, FilterQuery> mapBuilder = ImmutableMap.builder();

        for (EventFilterQueryProducer<? extends Event, ? extends FilterQuery> producer: this.producers.values()) {
            if (!producer.getEventType().isAssignableFrom(event.getClass())) {
                continue;
            }
            ((EventFilterQueryProducer<Event, FilterQuery>) producer)
                    .produce(event)
                    .ifPresent(f -> mapBuilder.put(producer.getFilterQueryType(), f));
        }
        return new CompoundFilterQuery(mapBuilder.build());
    }
}
