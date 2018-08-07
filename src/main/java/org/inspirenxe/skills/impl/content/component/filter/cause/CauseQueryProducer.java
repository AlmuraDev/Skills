package org.inspirenxe.skills.impl.content.component.filter.cause;

import com.google.common.collect.ImmutableList;
import org.inspirenxe.skills.impl.content.component.query.EventFilterQueryProducer;
import org.spongepowered.api.event.Event;

import java.util.Collection;

public class CauseQueryProducer implements EventFilterQueryProducer<Event, CauseQuery> {

    @Override
    public Class<Event> getEventType() {
        return Event.class;
    }

    @Override
    public Collection<CauseQuery> produce(Event source) {
        return ImmutableList.of(new CauseQuery(source.getCause()));
    }
}
