package org.inspirenxe.skills.impl.content.component.apply.data;

import com.almuradev.droplet.registry.Registry;
import com.almuradev.droplet.registry.RegistryKey;
import com.google.inject.Inject;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.inspirenxe.skills.impl.content.component.apply.MathOperationType;
import org.inspirenxe.skills.impl.content.component.apply.math.MathOperation;
import org.spongepowered.api.data.key.Key;

import java.util.Optional;

public class DataApplicatorParser implements Parser<DataApplicator> {

    @Inject private Registry<Key> registry;
    @Inject private Parser<MathOperation> mathOperationParser;
    @Inject private Parser<RegistryKey> keyParser;
    @Inject private Parser<String> stringParser;
    @Inject private Parser<KeyValue> keyValueParser;
    @Inject private Parser<MathOperationType> mathOperationTypeParser;

    @NonNull
    @Override
    public DataApplicator throwingParse(@NonNull Node node) throws XMLException {
        KeyValue keyValue = this.keyValueParser.throwingParse(node);
        Optional<Node> op = node.attribute("op").optional();
        MathOperationType operationType = null;
        if (op.isPresent()) {
            if (keyValue.getValue() != null && keyValue.getValue() instanceof Number) {
                operationType = this.mathOperationTypeParser.parse(op.get());
            } else {
                throw new XMLException(String.format("Cannot specify 'op' in conjunciton with non-Number value %s for key %s ", keyValue.getValue(), keyValue.getKey()));
            }
        }
        return new DataApplicator(keyValue, operationType);
    }
}
