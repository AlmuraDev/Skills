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
package org.inspirenxe.skills.impl.skill.builtin;

import com.almuradev.toolbox.inject.ToolboxBinder;
import net.kyori.violet.AbstractModule;
import org.inspirenxe.skills.api.skill.builtin.BlockCreationTracker;
import org.inspirenxe.skills.api.skill.builtin.EventProcessor;
import org.inspirenxe.skills.impl.skill.builtin.event.EventFilterProcessor;
import org.inspirenxe.skills.impl.skill.builtin.event.BlockCreationTrackerImpl;
import org.inspirenxe.skills.impl.skill.builtin.event.EventProcessorModule;
import org.inspirenxe.skills.impl.skill.builtin.registry.module.EventProcessorRegistryModule;

public final class BuiltinModule extends AbstractModule implements ToolboxBinder {

    @Override
    protected void configure() {
        this.install(new EventProcessorModule());

        this.bind(BlockCreationTracker.class).to(BlockCreationTrackerImpl.class);

        this.registry()
            .module(EventProcessor.class, EventProcessorRegistryModule.class);

        this.facet()
            .add(BlockCreationTrackerImpl.class)
            .add(EventFilterProcessor.class);
    }
}
