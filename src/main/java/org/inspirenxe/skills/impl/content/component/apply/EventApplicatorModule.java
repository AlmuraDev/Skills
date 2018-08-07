package org.inspirenxe.skills.impl.content.component.apply;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import net.kyori.feature.parser.FeatureDefinitionParserBinder;
import net.kyori.xml.node.parser.Parser;
import net.kyori.xml.node.parser.ParserBinder;
import org.inspirenxe.skills.impl.content.component.apply.cancel.EventCancelParser;
import org.inspirenxe.skills.impl.content.component.apply.economy.EconomyApplicatorParser;
import org.inspirenxe.skills.impl.content.component.apply.experience.ExperienceApplicatorParser;
import org.inspirenxe.skills.impl.content.component.apply.math.BigDecimalParser;
import org.inspirenxe.skills.impl.content.component.apply.math.MathOperation;
import org.inspirenxe.skills.impl.content.component.apply.math.MathOperationParser;
import org.inspirenxe.skills.impl.content.component.apply.message.Message;
import org.inspirenxe.skills.impl.content.component.apply.message.MessageApplicatorParser;
import org.inspirenxe.skills.impl.content.component.apply.message.MessageParser;

import java.math.BigDecimal;

public class EventApplicatorModule extends AbstractModule {

    @Override
    protected void configure() {

        final ParserBinder parsers = new ParserBinder(this.binder());
        parsers.bindParser(BigDecimal.class).to(BigDecimalParser.class);
        parsers.bindParser(MathOperation.class).to(MathOperationParser.class);
        parsers.bindParser(Message.class).to(MessageParser.class);

        final MapBinder<String, Parser<? extends EventApplicator>> applicators = MapBinder.newMapBinder(this.binder(), new TypeLiteral<String>() {}, new TypeLiteral<Parser<? extends EventApplicator>>() {});

        applicators.addBinding("cancel-event").to(EventCancelParser.class);
        applicators.addBinding("experience").to(ExperienceApplicatorParser.class);
        applicators.addBinding("economy-modifier").to(EconomyApplicatorParser.class);
        applicators.addBinding("message").to(MessageApplicatorParser.class);


        parsers.bindParser(EventApplicator.class).to(EventApplicatorParser.class);

        final FeatureDefinitionParserBinder features = new FeatureDefinitionParserBinder(this.binder());
        features.bindFeatureParser(EventApplicator.class).to(EventApplicatorParser.class);

    }
}
