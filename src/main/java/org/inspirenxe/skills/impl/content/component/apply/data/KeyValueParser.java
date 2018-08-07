package org.inspirenxe.skills.impl.content.component.apply.data;

import com.almuradev.droplet.registry.Registry;
import com.almuradev.droplet.registry.RegistryKey;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.data.key.Key;

@Singleton
public class KeyValueParser implements Parser<KeyValue> {

    @Inject private Registry<Key> registry;
    @Inject private Parser<RegistryKey> keyParser;
    @Inject private Parser<String> stringParser;

    @NonNull
    @Override
    public KeyValue throwingParse(@NonNull Node node) throws XMLException {
        Key<?> key = this.registry.ref(this.keyParser.throwingParse(node.requireAttribute("key"))).get();
        String value = node.attribute("value").optional().map(stringParser::parse).orElse(null);
        return new KeyValue(key, value);
    }
}
