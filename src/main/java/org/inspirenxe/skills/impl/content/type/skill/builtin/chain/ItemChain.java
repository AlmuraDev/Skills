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
package org.inspirenxe.skills.impl.content.type.skill.builtin.chain;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.impl.SkillsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("unchecked")
public final class ItemChain extends Chain<ItemChain> {

    private static final Logger logger = LoggerFactory.getLogger(SkillsImpl.ID);
    public List<ItemStack> toQuery = new ArrayList<>();
    public boolean inverseQuery = false, matchOnlyType = false;

    public ItemChain inverseQuery() {
        if (this.inErrorState) {
            return this;
        }
        this.inverseQuery = true;
        return this;
    }

    public ItemChain query(final String id) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(id);
        final ItemType itemType = Sponge.getRegistry().getType(ItemType.class, id).orElse(null);
        if (itemType == null) {
            logger.error("Unknown item id '" + id + "' given to item chain!");
            this.inErrorState = true;
            return this;
        }
        this.toQuery.add(ItemStack.of(itemType, 1));
        return this;
    }

    public ItemChain queryDomain(final String domain) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(domain);
        Sponge.getRegistry().getAllFor(domain, ItemType.class).forEach(type -> this.toQuery.add(ItemStack.of(type, 1)));
        return this;
    }

    public ItemChain query(final ItemType value) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(value);
        this.toQuery.add(ItemStack.of(value, 1));
        return this;
    }

    public ItemChain query(final ItemStack value) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(value);
        this.toQuery.add(value);
        return this;
    }

    public ItemChain query(final ItemStack... values) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(values);
        this.toQuery.addAll(Arrays.asList(values));
        return this;
    }

    public ItemChain matchTypeOnly() {
        if (this.inErrorState) {
            return this;
        }
        this.matchOnlyType = true;
        return this;
    }

    public ItemChain fuzzyMatch() {
        if (this.inErrorState) {
            return this;
        }
        this.matchOnlyType = false;
        return this;
    }

    /**
     * @return True if a stack of the given type could possibly match this chain (used to index chains by item type).
     */
    public boolean couldMatchType(final ItemType type) {
        if (this.inverseQuery) {
            return true;
        }
        for (final ItemStack query : this.toQuery) {
            if (query.getType() == type) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return True if the given stack matches this chain, honoring {@link #matchOnlyType} and {@link #inverseQuery}.
     */
    public boolean matches(final ItemStack stack) {
        final Comparator<ItemStack> comparator = this.matchOnlyType ? ItemStackComparators.TYPE : ItemStackComparators.IGNORE_SIZE;
        boolean found = false;
        for (final ItemStack query : this.toQuery) {
            if (comparator.compare(query, stack) == 0) {
                found = true;
                break;
            }
        }
        return this.inverseQuery != found;
    }

    @Override
    public ItemChain from(final ItemChain builder) {
        super.from(builder);

        if (this.inErrorState) {
            return this;
        }

        this.matchOnlyType = builder.matchOnlyType;

        return this;
    }
}