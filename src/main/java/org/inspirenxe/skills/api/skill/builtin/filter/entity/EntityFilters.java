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
package org.inspirenxe.skills.api.skill.builtin.filter.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys;
import org.inspirenxe.skills.api.skill.builtin.entity.FuzzyEntity;
import org.inspirenxe.skills.api.skill.builtin.filter.FuzzyMatchableFilter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class EntityFilters {

    private static final FuzzyMatchableFilter<Entity, FuzzyEntity> ALL_TYPES;

    static {
        final Set<FuzzyEntity> entities = new HashSet<>();
        for (EntityType type : Sponge.getRegistry().getAllOf(EntityType.class)) {
            entities.add(FuzzyEntity.type(type));
        }

        ALL_TYPES = new FuzzyMatchableFilter<>(SkillsEventContextKeys.PROCESSING_ENTITY, entities);
    }

    private EntityFilters() {
    }

    public static FuzzyMatchableFilter<Entity, FuzzyEntity> entities(final String... value) {
        checkNotNull(value);

        final Set<FuzzyEntity> entities = new HashSet<>();
        for (String id : value) {
            final EntityType type = Sponge.getRegistry().getType(EntityType.class, id).orElse(null);
            if (type == null) {
                // TODO Message
            } else {
                entities.add(FuzzyEntity.type(type));
            }
        }
        return new FuzzyMatchableFilter<>(SkillsEventContextKeys.PROCESSING_ENTITY, entities);
    }

    public static FuzzyMatchableFilter<Entity, FuzzyEntity> entities(final EntityType... value) {
        checkNotNull(value);

        final Set<FuzzyEntity> entities = new HashSet<>();
        for (EntityType type : value) {
            entities.add(FuzzyEntity.type(type));
        }

        return new FuzzyMatchableFilter<>(SkillsEventContextKeys.PROCESSING_ENTITY, entities);
    }

    public static FuzzyMatchableFilter<Entity, FuzzyEntity> entitiesFor(final String... value) {
        checkNotNull(value);

        final Set<FuzzyEntity> entities = new HashSet<>();
        for (String id : value) {
            final Collection<EntityType> types = Sponge.getRegistry().getAllFor(id, EntityType.class);
            if (types.isEmpty()) {
                // TODO Message
            } else {
                types.forEach(type -> entities.add(FuzzyEntity.type(type)));
            }
        }

        return new FuzzyMatchableFilter<>(SkillsEventContextKeys.PROCESSING_ENTITY, entities);
    }

    public static FuzzyMatchableFilter<Entity, FuzzyEntity> entities() {
        return ALL_TYPES;
    }

    public static FuzzyMatchableFilter<Entity, FuzzyEntity> entities(final FuzzyEntity... stacks) {
        return new FuzzyMatchableFilter<>(SkillsEventContextKeys.PROCESSING_ENTITY, Sets.newHashSet(checkNotNull(stacks)));
    }
}
