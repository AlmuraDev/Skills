package org.inspirenxe.skills.impl.content.component.filter;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import net.kyori.fragment.filter.Filter;
import net.kyori.fragment.filter.FilterBinder;
import net.kyori.xml.node.parser.Parser;

public class TypedMultiFilterBinder {

    private final MapBinder<String, Parser<? extends TypedMultiFilter<?>>> binder;

    public TypedMultiFilterBinder(final Binder binder) {
        this.binder = MapBinder.newMapBinder(binder, new TypeLiteral<String>() {}, new TypeLiteral<Parser<? extends TypedMultiFilter<?>>>() {});
    }

    public LinkedBindingBuilder<Parser<? extends TypedMultiFilter<?>>> bindFilter(final String key) {
        return this.binder.addBinding(key);
    }
}
