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
package org.inspirenxe.skills.impl;

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.kyori.membrane.facet.internal.Facets;
import org.inspirenxe.skills.api.event.DiscoverContentEvent;
import org.inspirenxe.skills.api.plugin.SkillsPlugin;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import javax.annotation.Nullable;

@Plugin(id = SkillsImpl.ID)
public final class SkillsImpl extends SkillsPlugin {

    public static final String ID = "skills";

    private final Path configDir;
    @Nullable private final Facets facets;

    @Inject
    public SkillsImpl(final Injector baseInjector, final Logger logger, @ConfigDir(sharedRoot = false) final Path configDir) throws IOException, URISyntaxException {
        super(SkillsImpl.ID, logger, configDir);
        this.configDir = configDir;
        this.writeDefaultAssets();

        this.facets = baseInjector.createChildInjector(new SkillsModule()).getInstance(Facets.class);
    }

    @Listener(order = Order.PRE)
    public void onGameConstruction(final GameConstructionEvent event) {
        if (this.facets != null) {
            this.facets.enable();
        }
    }

    @Listener
    public void onGameStopping(final GameStoppingEvent event) {
        if (this.facets != null) {
            this.facets.disable();
        }
    }

    @Listener
    public void onDiscoverContent(final DiscoverContentEvent event) {
        event.addSearchPath(this.configDir);
    }
}
