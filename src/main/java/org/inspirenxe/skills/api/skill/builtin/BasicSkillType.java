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
package org.inspirenxe.skills.api.skill.builtin;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.api.effect.firework.FireworkEffectType;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.SkillType;
import org.inspirenxe.skills.api.skill.holder.SkillHolderContainer;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class BasicSkillType implements SkillType {

    private final PluginContainer container;
    private final String id, name;
    private final LevelFunctionType levelFunction;
    private final Text formattedName;
    private final int maxLevel;
    private final Map<SkillHolderContainer, Map<EventProcessor, List<FilterRegistrar>>> eventRegistrations = new HashMap<>();

    protected BasicSkillType(final PluginContainer container, final String id, final String name, final Text formattedName,
        final LevelFunctionType levelFunction, final int maxLevel) {
        this.container = checkNotNull(container);
        this.id = container.getId() + ":" + id;
        this.name = checkNotNull(name);
        this.formattedName = checkNotNull(formattedName);
        this.levelFunction = checkNotNull(levelFunction);
        this.maxLevel = maxLevel;
    }

    @Override
    public final String getId() {
        return this.id;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final PluginContainer getPlugin() {
        return this.container;
    }

    @Override
    public final int getMaxLevel() {
        return this.maxLevel;
    }

    @Override
    public final LevelFunctionType getLevelFunction() {
        return this.levelFunction;
    }

    @Override
    public final Text getFormattedName() {
        return this.formattedName;
    }

    public final BasicSkillType register(final SkillHolderContainer container, final FilterRegistrar registrar, final EventProcessor... processors) {
        final Map<EventProcessor, List<FilterRegistrar>> processorMap = this.eventRegistrations
            .computeIfAbsent(checkNotNull(container, "SkillHolderContainer cannot be null!"), k -> new HashMap<>());

        for (EventProcessor processor : processors) {
            processorMap
                .computeIfAbsent(checkNotNull(processor, "EventFilterProcessor cannot be null!"), k -> new ArrayList<>())
                .add(checkNotNull(registrar, "FilterRegistrar cannot be null!"));
        }

        return this;
    }

    public final void configure(final Collection<SkillHolderContainer> containers) {
        this.eventRegistrations.clear();
        this.onConfigure(containers);
    }

    public List<FilterRegistrar> getFilterRegistrations(final SkillHolderContainer container, final EventProcessor processor) {
        checkNotNull(container);
        checkNotNull(processor);

        return this.eventRegistrations
            .getOrDefault(container, Collections.emptyMap())
            .getOrDefault(processor, Collections.emptyList());
    }

    protected abstract void onConfigure(final Collection<SkillHolderContainer> containers);

    public void onXPChanged(final Cause cause, final Skill skill, final double amount) {}

    public void onLevelChanged(final Cause cause, final Skill skill, final int newLevel) {}

    public Optional<FireworkEffectType> getFireworkEffectFor(final int level) {
        return Optional.empty();
    }
}
