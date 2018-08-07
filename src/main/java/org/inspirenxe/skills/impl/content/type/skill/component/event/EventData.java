package org.inspirenxe.skills.impl.content.type.skill.component.event;

import net.kyori.fragment.filter.FilterQuery;
import org.inspirenxe.skills.impl.content.component.query.EventFilterProducerRegistry;
import org.spongepowered.api.event.Event;

import java.util.Collection;

public class EventData {

    private final Event event;
    private final Collection<FilterQuery> queries;

    public EventData(Event event) {
        this.event = event;
        this.queries = EventFilterProducerRegistry.INSTANCE.getQueries(event);
    }

    public Event getEvent() {
        return this.event;
    }

    public Collection<FilterQuery> getQueries() {
        return this.queries;
    }

}
