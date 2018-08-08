package org.inspirenxe.skills.impl.content.component.filter;

import com.google.inject.binder.LinkedBindingBuilder;
import net.kyori.fragment.filter.FilterBinder;
import net.kyori.xml.node.parser.Parser;

public class TypedMultiFilterBinderWrapper {

    private final FilterBinder filterBinder;

    public TypedMultiFilterBinderWrapper(final FilterBinder filterBinder) {
        this.filterBinder = filterBinder;
    }

    @SuppressWarnings("unchecked")
    public LinkedBindingBuilder<Parser<? extends TypedMultiFilter<?>>> bindFilter(final String key) {
        // Cast to force users of this class to provide TypedMultiFilter parsers
        return (LinkedBindingBuilder) this.filterBinder.bindFilter(key);
    }
}
