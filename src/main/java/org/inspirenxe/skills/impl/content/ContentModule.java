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
package org.inspirenxe.skills.impl.content;

import com.almuradev.droplet.component.range.RangeModule;
import com.almuradev.droplet.content.configuration.ContentConfiguration;
import com.almuradev.droplet.content.feature.context.FeatureContext;
import com.almuradev.droplet.content.inject.DynamicProvider;
import com.almuradev.droplet.content.inject.GlobalBinder;
import com.almuradev.droplet.content.loader.ContentManager;
import com.almuradev.droplet.content.loader.ContentManagerImpl;
import com.almuradev.droplet.content.loader.finder.ContentFinder;
import com.almuradev.droplet.content.processor.GlobalProcessor;
import com.almuradev.toolbox.inject.ToolboxBinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import net.kyori.violet.AbstractModule;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.ParserModule;
import org.inspirenxe.skills.impl.content.loader.finder.ContentFinderImpl;
import org.inspirenxe.skills.impl.content.parser.ContentParserModule;
import org.inspirenxe.skills.impl.content.registry.RegistryModule;
import org.inspirenxe.skills.impl.content.type.ContentTypeModule;
import org.spongepowered.api.config.ConfigDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ContentModule extends AbstractModule implements ToolboxBinder, GlobalBinder {

    @Override
    protected void configure() {
        this.bindGlobalProcessor(DummyGlobalProcessor.class);

        this.bind(ContentManager.class).to(ContentManagerImpl.class);
        this.bind(net.kyori.feature.FeatureDefinitionContext.class).toProvider(new TypeLiteral<DynamicProvider<FeatureContext>>() {});
        this.bind(FeatureContext.class).toProvider(new TypeLiteral<DynamicProvider<FeatureContext>>() {});
        this.bind(new TypeLiteral<DynamicProvider<FeatureContext>>() {}).toInstance(new DynamicProvider<>());
        this.bind(ContentFinder.class).to(ContentFinderImpl.class);

        this.install(new ParserModule());
        this.install(new RangeModule());
        this.install(new ContentTypeModule());
        this.install(new ContentParserModule());
        this.install(new RegistryModule());

        this.facet()
            .add(ContentInstaller.class);
    }

    @Provides
    @Singleton
    ContentConfiguration configuration() {
        return new ContentConfiguration() {
            private final List<Path> searchPaths = new ArrayList<>();
            private final int maxDepth = 10;

            @Override
            public List<Path> searchPaths() {
                return this.searchPaths;
            }

            @Override
            public int maxDepth() {
                return this.maxDepth;
            }
        };
    }

    @Singleton
    private static class DummyGlobalProcessor implements GlobalProcessor {

        @Override
        public void process(final Node node) {

        }
    }
}
