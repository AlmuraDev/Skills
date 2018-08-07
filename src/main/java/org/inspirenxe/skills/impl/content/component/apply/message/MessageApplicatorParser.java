package org.inspirenxe.skills.impl.content.component.apply.message;

import com.google.inject.Inject;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MessageApplicatorParser implements Parser<MessageApplicator> {

    @Inject private MessageParser messageParser;

    @NonNull
    @Override
    public MessageApplicator throwingParse(@NonNull Node node) throws XMLException {
        return new MessageApplicator(this.messageParser.throwingParse(node));
    }
}
