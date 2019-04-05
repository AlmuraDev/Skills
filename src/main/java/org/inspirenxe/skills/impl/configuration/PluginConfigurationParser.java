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
package org.inspirenxe.skills.impl.configuration;

import com.google.inject.Inject;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.inspirenxe.skills.impl.configuration.container.ContainerShareConfiguration;
import org.inspirenxe.skills.impl.configuration.database.DatabaseConfiguration;

import java.util.HashMap;

public final class PluginConfigurationParser implements Parser<PluginConfiguration> {

    private final Parser<DatabaseConfiguration> databaseConfigurationParser;
    private final Parser<ContainerShareConfiguration> containerShareConfigurationParser;
    private final Parser<Integer> intParser;

    @Inject
    public PluginConfigurationParser(final Parser<DatabaseConfiguration> databaseConfigurationParser, final Parser<ContainerShareConfiguration>
        containerShareConfigurationParser, final Parser<Integer> intParser) {
        this.databaseConfigurationParser = databaseConfigurationParser;
        this.containerShareConfigurationParser = containerShareConfigurationParser;
        this.intParser = intParser;
    }

    @NonNull
    @Override
    public PluginConfiguration throwingParse(@NonNull final Node node) {
        final DatabaseConfiguration databaseConfiguration = this.databaseConfigurationParser.parse(node.element("database").required());

        ContainerShareConfiguration containerShareConfiguration;
        final Node containerNode = node.element("container-share").optional().orElse(null);

        if (containerNode == null) {
            containerShareConfiguration = new ContainerShareConfiguration(new HashMap<>());
        } else {
            containerShareConfiguration = this.containerShareConfigurationParser.parse(containerNode);
        }

        final Integer saveInterval = this.intParser.parse(node.element("save-interval").required());

        return new PluginConfiguration(databaseConfiguration, containerShareConfiguration, saveInterval);
    }
}
