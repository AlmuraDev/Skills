package org.inspirenxe.skills.impl.content.component.query;

import com.google.common.collect.ImmutableList;
import net.kyori.fragment.filter.FilterQuery;
import org.inspirenxe.skills.impl.content.component.filter.block.BlockQuery;
import org.inspirenxe.skills.impl.content.component.filter.block.BlockQueryProducer;
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
    }

    @SuppressWarnings("unchecked")
    public Collection<FilterQuery> getQueries(Event event) {
        ImmutableList.Builder<FilterQuery> listBuilder = ImmutableList.builder();
        for (EventFilterQueryProducer<? extends Event, ? extends FilterQuery> producer: this.producers.values()) {
            if (!producer.getEventType().isAssignableFrom(event.getClass())) {
                continue;
            }
            listBuilder.addAll(((EventFilterQueryProducer) producer).produce(event));
        }
        return listBuilder.build();
        //return ((EventFilterQueryProducer) producer).produce(producer.getEventType().cast(event));
    }
}
