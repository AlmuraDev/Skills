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
    public final boolean queryable(@NonNull FilterQuery query) {
        return query instanceof CompoundFilterQuery
                && ((CompoundFilterQuery) query).getQuery(this.filterQueryType)
                    .map(this::individualQueryabled).orElse(false);
    }

    @Override
    public final FilterResponse typedQuery(final @NonNull CompoundFilterQuery query) {
        return query.getQuery(this.getFilterQueryType()).map(this::individualQuery).orElse(FilterResponse.ABSTAIN);
    }

    protected abstract FilterResponse individualQuery(Q query);

    private boolean individualQueryabled(Q query) {
        return true;
    }
}
