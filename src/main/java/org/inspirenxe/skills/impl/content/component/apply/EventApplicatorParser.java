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
package org.inspirenxe.skills.impl.content.component.apply;

import com.google.inject.Singleton;
import net.kyori.feature.FeatureDefinitionContext;
import net.kyori.feature.parser.AbstractInjectedFeatureDefinitionParser;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

@Singleton
public final class EventApplicatorParser extends AbstractInjectedFeatureDefinitionParser<EventApplicator> implements Parser<EventApplicator> {

    private final Map<String, Parser<? extends EventApplicator>> parsers;
    private final Provider<FeatureDefinitionContext> featureContext;

    @Inject
    public EventApplicatorParser(final Map<String, Parser<? extends EventApplicator>> parsers,
      final Provider<FeatureDefinitionContext> featureContext) {
        this.parsers = parsers;
        this.featureContext = featureContext;
    }

    @Override
    protected EventApplicator realThrowingParse(@NonNull final Node node) throws XMLException {
        final Parser<? extends EventApplicator> parser = this.parsers.get(node.name());
        if (parser != null) {
            return this.featureContext.get().define(EventApplicator.class, node, parser.parse(node));
        }
        throw new XMLException("Could not find event applicator parser with name " + node.name());
    }
}
