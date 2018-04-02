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
package org.inspirenxe.skills.impl.content.type.block.lazy;

import com.almuradev.droplet.parser.Parser;
import com.almuradev.droplet.parser.ParserException;
import com.almuradev.droplet.registry.Registry;
import com.almuradev.droplet.registry.RegistryKey;
import com.almuradev.droplet.registry.reference.RegistryReference;
import net.kyori.xml.node.Node;
import org.inspirenxe.skills.impl.content.type.block.lazy.value.LazyStateValue;
import org.spongepowered.api.block.BlockType;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class LazyBlockStateParser implements Parser<LazyBlockState> {
  private final Registry<BlockType> registry;
  private final Parser<RegistryKey> keyParser;
  private final Parser<LazyStateValue<?>> propertyParser;

  @Inject
  private LazyBlockStateParser(final Registry<BlockType> registry, final Parser<RegistryKey> keyParser, final Parser<LazyStateValue<?>>
      propertyParser) {
    this.registry = registry;
    this.keyParser = keyParser;
    this.propertyParser = propertyParser;
  }

  @Override
  public LazyBlockState throwingParse(final Node node) throws ParserException {
    final RegistryReference<BlockType> block = this.block(node, node.attribute("block"));

    final Map<String, LazyStateValue<?>> properties = node.nodes()
      .filter(that -> !that.name().equals("block"))
      .map(property -> new AbstractMap.SimpleImmutableEntry<>(
        property.name(),
        this.propertyParser.parse(property)
      ))
      .filter(property -> property.getValue() != null)
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue
      ));
    if(properties.isEmpty()) {
      return new StatelessLazyBlockState(block);
    }
    return new StatefulLazyBlockState(block, properties);
  }

  private RegistryReference<BlockType> block(final Node node, final Optional<Node> attribute) throws ParserException {
    if(attribute.isPresent()) {
      return this.registry.ref(this.keyParser.parse(attribute.get()));
    } else if(node.value().indexOf(':') > -1) {
      return this.registry.ref(this.keyParser.parse(node));
    }
    throw new ParserException("Could not parse lazy block state");
  }
}
