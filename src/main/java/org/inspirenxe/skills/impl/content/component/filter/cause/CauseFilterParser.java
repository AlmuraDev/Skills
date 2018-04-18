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
package org.inspirenxe.skills.impl.content.component.filter.cause;

import com.almuradev.droplet.component.filter.FilterTypeParser;
import com.almuradev.droplet.parser.Parser;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import org.inspirenxe.skills.impl.cause.CauseOperatorType;
import org.inspirenxe.skills.impl.cause.CauseType;

@Singleton
public final class CauseFilterParser implements FilterTypeParser<CauseFilter> {

  private final Parser<CauseOperatorType> causeOperatorParser;
  private final Parser<CauseType> causeTypeParser;

  @Inject
  public CauseFilterParser(final Parser<CauseOperatorType> causeOperatorParser, final Parser<CauseType> causeTypeParser) {
    this.causeOperatorParser = causeOperatorParser;
    this.causeTypeParser = causeTypeParser;
  }

  @Override
  public CauseFilter throwingParse(Node node) throws XMLException {
    final CauseOperatorType causeOperatorType = this.causeOperatorParser.parse(node.requireAttribute("op"));
    final CauseType causeType = this.causeTypeParser.parse(node.requireAttribute("type"));
    return new CauseFilter(causeOperatorType, causeType);
  }
}
