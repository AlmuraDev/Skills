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
package org.inspirenxe.skills.impl.parser.lazy.item;

import com.almuradev.droplet.parser.Nodes;
import com.almuradev.droplet.parser.Parser;
import com.almuradev.droplet.registry.Registry;
import com.almuradev.droplet.registry.RegistryKey;
import com.almuradev.droplet.registry.reference.RegistryReference;
import com.google.common.collect.MoreCollectors;
import net.kyori.xml.node.Node;
import org.spongepowered.api.item.ItemType;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class LazyItemStackParser implements Parser<LazyItemStack> {

  private final Registry<ItemType> registry;
  private final Parser<RegistryKey> keyParser;
  private final Parser<Integer> intParser;

  @Inject
  private LazyItemStackParser(final Registry<ItemType> registry, final Parser<RegistryKey> keyParser, final Parser<Integer> intParser) {
    this.registry = registry;
    this.keyParser = keyParser;
    this.intParser = intParser;
  }

  @Override
  public LazyItemStack throwingParse(final Node node) {
    final RegistryReference<ItemType> item = this.registry.ref(this.keyParser.parse(Nodes.firstNonEmpty(node, "item")));
    @Deprecated final int data = node.nodes("data").collect(MoreCollectors.toOptional()).map(this.intParser::parse).orElse(LazyItemStack.DEFAULT_DATA);
    final int quantity = node.nodes("quantity").collect(MoreCollectors.toOptional()).map(this.intParser::parse).orElse(LazyItemStack.DEFAULT_QUANTITY);
    return new LazyItemStackImpl(item, data, quantity);
  }
}
