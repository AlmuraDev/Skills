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
import static org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys.PROCESSING_ITEM;
import static org.inspirenxe.skills.api.skill.builtin.filter.entity.EntityFilters.entities;
import static org.inspirenxe.skills.api.skill.builtin.filter.item.ItemFilters.items;
import static org.spongepowered.api.entity.EntityTypes.ITEM;

import com.google.common.collect.Sets;
import net.kyori.filter.Filter;
import net.kyori.filter.Filters;
import org.inspirenxe.skills.api.skill.builtin.filter.FuzzyMatchableFilter;
import org.inspirenxe.skills.api.skill.builtin.inventory.FuzzyItemStack;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class DropFilters {

    private DropFilters() {
    }

    public static Filter drops(final String... value) {
        checkNotNull(value);

        final Set<FuzzyItemStack> items = new HashSet<>();
        for (String id : value) {
            final ItemType type = Sponge.getRegistry().getType(ItemType.class, id).orElse(null);
            if (type == null) {
                // TODO Message
            } else {
                items.add(FuzzyItemStack.type(type));
            }
        }
        return Filters.all(entities(ITEM), new FuzzyMatchableFilter<>(PROCESSING_ITEM, items));
    }

    public static Filter drops(final ItemType... value) {
        checkNotNull(value);

        final Set<FuzzyItemStack> items = new HashSet<>();
        for (ItemType type : value) {
            items.add(FuzzyItemStack.type(type));
        }

        return new FuzzyMatchableFilter<>(PROCESSING_ITEM, items);
    }

    public static Filter dropsFor(final String... value) {
        checkNotNull(value);

        final Set<FuzzyItemStack> drops = new HashSet<>();
        for (String id : value) {
            final Collection<ItemType> types = Sponge.getRegistry().getAllFor(id, ItemType.class);
            if (types.isEmpty()) {
                // TODO Message
            } else {
                types.forEach(type -> drops.add(FuzzyItemStack.type(type)));
            }
        }

        return Filters.all(entities(ITEM), new FuzzyMatchableFilter<>(PROCESSING_ITEM, drops));
    }

    public static Filter drops() {
        return Filters.all(entities(ITEM), items());
    }

    public static Filter dropStacks(final FuzzyItemStack... stacks) {
        return Filters.all(entities(ITEM), new FuzzyMatchableFilter<>(PROCESSING_ITEM, Sets.newHashSet(checkNotNull(stacks))));
    }
}
