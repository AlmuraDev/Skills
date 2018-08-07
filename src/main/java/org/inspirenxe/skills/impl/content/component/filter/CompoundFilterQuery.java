package org.inspirenxe.skills.impl.content.component.filter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import net.kyori.fragment.filter.FilterQuery;
import org.inspirenxe.skills.impl.content.type.skill.component.event.EventData;

import java.util.Collection;
import java.util.Optional;

public class CompoundFilterQuery implements FilterQuery {

    private final ImmutableMap<Class<? extends FilterQuery>, FilterQuery> filterQueries;

    public CompoundFilterQuery(ImmutableMap<Class<? extends FilterQuery>, FilterQuery> filterQueries) {
        this.filterQueries = filterQueries;
    }

    public <T extends FilterQuery> Optional<T> getQuery(Class<T> filterClass) {
        return Optional.ofNullable(filterClass.cast(this.filterQueries.get(filterClass)));
    }

    public Collection<FilterQuery> getAll() {
        return this.filterQueries.values();
    }

}
