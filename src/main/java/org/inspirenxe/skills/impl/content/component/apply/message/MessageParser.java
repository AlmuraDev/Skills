package org.inspirenxe.skills.impl.content.component.apply.message;

import com.google.inject.Singleton;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.serializer.TextSerializers;

@Singleton
public class MessageParser implements Parser<Message> {

    @NonNull
    @Override
    public Message throwingParse(@NonNull Node node) throws XMLException {
        Text text = TextSerializers.LEGACY_FORMATTING_CODE.deserialize(node.requireAttribute("text").value().toUpperCase());
        String key = node.requireAttribute("key").value();
        ChatType type = Sponge.getRegistry().getType(ChatType.class, key)
                .orElseThrow(() -> new IllegalArgumentException("Unknown ChatType " + key));

        return new Message(text, type);
    }
}
