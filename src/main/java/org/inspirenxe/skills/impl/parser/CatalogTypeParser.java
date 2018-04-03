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
package org.inspirenxe.skills.impl.parser;

import com.almuradev.droplet.parser.Parser;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.GameRegistry;

import javax.inject.Inject;

@Singleton
public final class CatalogTypeParser<C extends CatalogType> implements Parser<C> {

  private final GameRegistry registry;
  private final TypeLiteral<C> type;

  @Inject
  private CatalogTypeParser(final GameRegistry registry, final TypeLiteral<C> type) {
    this.registry = registry;
    this.type = type;
  }

  @Override
  public C throwingParse(final Node node) throws XMLException {
    return this.registry.getType((Class<C>) this.type.getRawType(), node.value())
        .orElseThrow(() -> new XMLException("Could not parse " + this.type.getRawType()));
  }
}