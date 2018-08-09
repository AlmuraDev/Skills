package org.inspirenxe.skills.impl.content.component.apply.cancel;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import net.kyori.xml.node.stream.NodeStreamElement;
import org.checkerframework.checker.nullness.qual.NonNull;

@Singleton
public class TransactionCancelParser implements Parser<TransactionCancelApplicator> {

    @Inject private Parser<Boolean> booleanParser;

    @NonNull
    @Override
    public TransactionCancelApplicator throwingParse(@NonNull Node node) throws XMLException {
        final NodeStreamElement<Node> value = node.attribute("value");
        final boolean cancelled = value.optional().map(this.booleanParser::parse).orElse(true);

        return new TransactionCancelApplicator(cancelled);
    }
}
