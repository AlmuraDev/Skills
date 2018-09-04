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

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.impl.content.type.skill.builtin.ChainBuilder;
import org.inspirenxe.skills.impl.util.function.TriConsumer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public final class ItemChainBuilder extends ChainBuilder<ItemChainBuilder> {

    public List<ItemStack> toQuery = new ArrayList<>();
    public boolean excludeQuery = false, matchOnlyType = false;
    public TriConsumer<Player, Skill, Integer> denyLevelRequired;

    public ItemChainBuilder excludeQuery() {
        this.excludeQuery = true;
        return this;
    }

    public ItemChainBuilder query(final ItemStack value) {
        checkNotNull(value);
        this.toQuery.add(value);
        return this;
    }

    public ItemChainBuilder query(final ItemStack... values) {
        checkNotNull(values);
        this.toQuery.addAll(Arrays.asList(values));
        return this;
    }

    public ItemChainBuilder matchTypeOnly() {
        this.matchOnlyType = true;
        return this;
    }

    public ItemChainBuilder denyLevelRequired(final TriConsumer<Player, Skill, Integer> value) {
        this.denyLevelRequired = value;
        return this;
    }

    @Override
    public ItemChainBuilder from(final ItemChainBuilder builder) {
        this.matchOnlyType = builder.matchOnlyType;
        this.denyLevelRequired = builder.denyLevelRequired;

        return this;
    }
}