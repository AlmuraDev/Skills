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
package org.inspirenxe.skills.api.skill.builtin.filter.item;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import org.inspirenxe.skills.api.skill.builtin.inventory.FuzzyItemStack;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class ItemFilters {

    private static final ItemFuzzyFilter ALL_TYPES;

    static {
        final Set<FuzzyItemStack> stacks = new HashSet<>();
        for (ItemType type : Sponge.getRegistry().getAllOf(ItemType.class)) {
            stacks.add(FuzzyItemStack.type(type));
        }

        ALL_TYPES = new ItemFuzzyFilter(stacks);
    }

    private ItemFilters() {
    }

    public static ItemFuzzyFilter items(final String... value) {
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
        return new ItemFuzzyFilter(items);
    }

    public static ItemFuzzyFilter items(final ItemType... value) {
        checkNotNull(value);

        final Set<FuzzyItemStack> items = new HashSet<>();
        for (ItemType type : value) {
            items.add(FuzzyItemStack.type(type));
        }

        return new ItemFuzzyFilter(items);
    }

    public static ItemFuzzyFilter itemTypesFor(final String... value) {
        checkNotNull(value);

        final Set<FuzzyItemStack> items = new HashSet<>();
        for (String id : value) {
            final Collection<ItemType> types = Sponge.getRegistry().getAllFor(id, ItemType.class);
            if (types.isEmpty()) {
                // TODO Message
            } else {
                types.forEach(type -> items.add(FuzzyItemStack.type(type)));
            }
        }

        return new ItemFuzzyFilter(items);
    }

    public static ItemFuzzyFilter items() {
        return ALL_TYPES;
    }

    public static ItemFuzzyFilter itemStacks(final FuzzyItemStack... stacks) {
        return new ItemFuzzyFilter(Sets.newHashSet(checkNotNull(stacks)));
    }
}
