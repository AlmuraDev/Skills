package org.inspirenxe.skills.impl.content.component.apply;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import net.kyori.feature.parser.FeatureDefinitionParserBinder;
import net.kyori.xml.node.parser.Parser;
import net.kyori.xml.node.parser.ParserBinder;
import org.inspirenxe.skills.impl.content.component.apply.cancel.EventCancelParser;

public class EventApplicatorModule extends AbstractModule {

    @Override
    protected void configure() {
        final ParserBinder parsers = new ParserBinder(this.binder());
        parsers.bindParser(EventApplicator.class).to(EventApplicatorParser.class);

        final FeatureDefinitionParserBinder features = new FeatureDefinitionParserBinder(this.binder());
        features.bindFeatureParser(EventApplicator.class).to(EventApplicatorParser.class);

        final MapBinder<String, Parser<? extends EventApplicator>> applicators = MapBinder.newMapBinder(this.binder(), new TypeLiteral<String>() {}, new TypeLiteral<Parser<? extends EventApplicator>>() {});
        applicators.addBinding("cancel").to(EventCancelParser.class);

    }
}
