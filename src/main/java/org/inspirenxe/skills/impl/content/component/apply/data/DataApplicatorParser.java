/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
    public DataApplicator throwingParse(@NonNull final Node node) throws XMLException {
        final KeyValue keyValue = this.keyValueParser.throwingParse(node);
        final Optional<Node> op = node.attribute("op").optional();
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
