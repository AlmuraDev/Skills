package org.inspirenxe.skills.impl.content.component.filter.cause;

import com.google.common.collect.ImmutableList;
import org.inspirenxe.skills.impl.content.component.query.EventFilterQueryProducer;
import org.spongepowered.api.event.Event;

import java.util.Collection;
import java.util.Optional;

public class CauseQueryProducer implements EventFilterQueryProducer<Event, CauseQuery> {

    @Override
    public Class<CauseQuery> getFilterQueryType() {
        return CauseQuery.class;
    }

    @Override
    public Class<Event> getEventType() {
        return Event.class;
    }

    @Override
    public Optional<CauseQuery> produce(Event source) {
        return Optional.of(new CauseQuery(source.getCause()));
    }
}
