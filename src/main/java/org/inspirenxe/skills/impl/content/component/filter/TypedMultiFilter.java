package org.inspirenxe.skills.impl.content.component.filter;

import net.kyori.fragment.filter.FilterQuery;
import net.kyori.fragment.filter.FilterResponse;
import net.kyori.fragment.filter.TypedFilter;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class TypedMultiFilter<Q extends FilterQuery> implements TypedFilter<CompoundFilterQuery> {

    private final Class<Q> filterQueryType;

    protected TypedMultiFilter(Class<Q> filterQueryType) {
        this.filterQueryType = filterQueryType;
    }

    public Class<Q> getFilterQueryType() {
        return this.filterQueryType;
    }

    @Override
    public boolean queryable(@NonNull FilterQuery query) {
        return query instanceof CompoundFilterQuery;
    }

    @Override
    public FilterResponse typedQuery(final @NonNull CompoundFilterQuery query) {
        return null;
    }
}
