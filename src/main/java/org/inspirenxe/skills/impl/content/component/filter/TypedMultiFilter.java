package org.inspirenxe.skills.impl.content.component.filter;

import net.kyori.fragment.filter.FilterQuery;
import net.kyori.fragment.filter.FilterResponse;
import net.kyori.fragment.filter.TypedFilter;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class TypedMultiFilter<Q extends FilterQuery> implements TypedFilter<EventCompoundFilterQuery> {

    private final Class<Q> filterQueryType;

    protected TypedMultiFilter(Class<Q> filterQueryType) {
        this.filterQueryType = filterQueryType;
    }

    public Class<Q> getFilterQueryType() {
        return this.filterQueryType;
    }

    @Override
    public final boolean queryable(@NonNull FilterQuery query) {
        return query instanceof EventCompoundFilterQuery
                && ((EventCompoundFilterQuery) query).getQuery(this.filterQueryType)
                    .map(this::individualQueryabled).orElse(false);
    }

    @Override
    public final FilterResponse typedQuery(final @NonNull EventCompoundFilterQuery query) {
        return query.getQuery(this.getFilterQueryType()).map(q -> this.individualQuery(query, q)).orElse(FilterResponse.ABSTAIN);
    }

    protected abstract FilterResponse individualQuery(EventCompoundFilterQuery parent, Q query);

    private boolean individualQueryabled(Q query) {
        return true;
    }
}
