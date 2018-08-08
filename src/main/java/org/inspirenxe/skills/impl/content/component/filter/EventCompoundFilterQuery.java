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

    public EventCompoundFilterQuery(Event event, SkillType skillType) {
        this.event = event;
        this.filterQueries = EventFilterProducerRegistry.INSTANCE.getQueries(event);
        this.skillType = skillType;
    }

    public <T extends FilterQuery> Optional<T> getQuery(Class<T> filterClass) {
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
