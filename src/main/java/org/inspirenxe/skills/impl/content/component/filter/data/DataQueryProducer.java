package org.inspirenxe.skills.impl.content.component.filter.data;

import org.inspirenxe.skills.impl.content.component.filter.cause.CauseQuery;
import org.inspirenxe.skills.impl.content.component.query.EventFilterQueryProducer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.event.Event;

import java.util.Optional;

public class DataQueryProducer implements EventFilterQueryProducer<Event, DataQuery> {

    @Override
    public Class<DataQuery> getFilterQueryType() {
        return DataQuery.class;
    }

    @Override
    public Class<Event> getEventType() {
        return Event.class;
    }

    @Override
    public Optional<DataQuery> produce(Event source) {
        return source.getCause().first(DataHolder.class).map(DataQuery::new);
    }
}
