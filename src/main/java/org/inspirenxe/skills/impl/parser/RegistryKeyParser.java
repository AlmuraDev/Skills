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

import com.almuradev.droplet.parser.ParserException;
import com.almuradev.droplet.parser.primitive.PrimitiveParser;
import com.almuradev.droplet.registry.RegistryKey;
import net.kyori.xml.node.Node;
import org.inspirenxe.skills.impl.registry.CatalogKey;

import javax.inject.Singleton;

@Singleton
public final class RegistryKeyParser implements PrimitiveParser<RegistryKey> {

  private static void requireNamespaced(final String string) throws ParserException {
    if (string.indexOf(':') == -1) {
      throw new ParserException("Expected namespaced string");
    }
  }

  @Override
  public RegistryKey parse(final Node node, final String string) throws ParserException {
    requireNamespaced(string);
    return new CatalogKey(string);
  }
}
