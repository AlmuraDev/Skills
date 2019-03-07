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
import org.inspirenxe.skills.impl.content.type.skill.builtin.query.Query;
import org.inspirenxe.skills.impl.content.type.skill.builtin.query.item.ItemQueries;
import org.inspirenxe.skills.impl.content.type.skill.builtin.query.item.ItemQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public final class ItemChain extends Chain<ItemChain> {

    public static ItemChain itemChain() {
        return new ItemChain();
    }

    public static ItemChain fromItemChain(ItemChain builder) {
        final ItemChain newChain = new ItemChain();

        newChain.level = builder.level;
        newChain.xp = builder.xp;
        newChain.economy = builder.economy;
        newChain.denyLevelRequired = builder.denyLevelRequired;
        newChain.inverseQuery = builder.inverseQuery;

        return newChain;
    }

    public boolean inverseQuery = false;
    public ItemQuery query = ItemQueries.DEFAULT;

    private ItemChain() {}

    public ItemChain inverseQuery() {
        if (this.inErrorState) {
            return this;
        }
        this.inverseQuery = true;
        return this;
    }

    public ItemChain query(final ItemQuery query) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(query);
        this.query = query;
        return this;
    }
}