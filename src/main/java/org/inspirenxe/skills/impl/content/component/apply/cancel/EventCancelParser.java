package org.inspirenxe.skills.impl.content.component.apply.cancel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import net.kyori.xml.node.stream.NodeStreamElement;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public class EventCancelParser implements Parser<EventCancelApplicator> {

    private final Parser<Boolean> booleanParser;

    @Inject
    public EventCancelParser(final Parser<Boolean> booleanParser) {
        this.booleanParser = booleanParser;
    }

    @NonNull
    @Override
    public EventCancelApplicator throwingParse(@NonNull Node node) throws XMLException {
        NodeStreamElement<Node> value = node.attribute("value");
        boolean cancelled = value.optional().map(booleanParser::parse).orElse(true);

        return new EventCancelApplicator(cancelled);
    }
}
