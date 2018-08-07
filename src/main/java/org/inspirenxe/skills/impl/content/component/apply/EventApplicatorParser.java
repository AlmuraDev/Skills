package org.inspirenxe.skills.impl.content.component.apply;

import com.google.inject.Singleton;
import net.kyori.feature.FeatureDefinitionContext;
import net.kyori.feature.parser.AbstractInjectedFeatureDefinitionParser;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

@Singleton
public final class EventApplicatorParser extends AbstractInjectedFeatureDefinitionParser<EventApplicator> implements Parser<EventApplicator> {

    private final Map<String, Parser<? extends EventApplicator<?>>> parsers;
    private Provider<FeatureDefinitionContext> featureContext;

    @Inject
    public EventApplicatorParser(final Map<String, Parser<? extends EventApplicator<?>>> parsers,
            Provider<FeatureDefinitionContext> featureContext) {
        this.parsers = parsers;
        this.featureContext = featureContext;
    }

    @Override
    protected EventApplicator<?> realThrowingParse(@NonNull Node node) throws XMLException {
        final Parser<? extends EventApplicator<?>> parser = this.parsers.get(node.name());
        if (parser != null) {
            return this.featureContext.get().define(EventApplicator.class, node, parser.parse(node));
        }
        throw new XMLException("Could not find event applicator parser with name " + node.name());
    }
}
