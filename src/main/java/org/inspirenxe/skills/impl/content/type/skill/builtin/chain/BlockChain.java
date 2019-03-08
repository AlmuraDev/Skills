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

import org.inspirenxe.skills.impl.content.type.skill.builtin.filter.creator.CreatorFilter;
import org.inspirenxe.skills.impl.content.type.skill.builtin.filter.creator.CreatorFilters;
import org.inspirenxe.skills.impl.content.type.skill.builtin.query.block.BlockQueries;
import org.inspirenxe.skills.impl.content.type.skill.builtin.query.block.BlockQuery;

@SuppressWarnings("unchecked")
public final class BlockChain extends Chain<BlockChain> {

    public static BlockChain blockChain() {
        return new BlockChain();
    }

    public static BlockChain fromBlockChain(BlockChain builder) {
        final BlockChain newChain = new BlockChain();

        newChain.level = builder.level;
        newChain.xp = builder.xp;
        newChain.economy = builder.economy;
        newChain.denyLevelRequired = builder.denyLevelRequired;
        newChain.inverseQuery = builder.inverseQuery;
        newChain.creator = builder.creator;

        return newChain;
    }

    public boolean inverseQuery = false;
    public BlockQuery query = BlockQueries.DEFAULT;
    public CreatorFilter creator = CreatorFilters.ANY;

    private BlockChain() {}

    public BlockChain inverseQuery() {
        if (this.inErrorState) {
            return this;
        }
        this.inverseQuery = true;
        return this;
    }

    public BlockChain query(final BlockQuery query) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(query);
        this.query = query;
        return this;
    }

    public BlockChain creator(final CreatorFilter value) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(value);
        this.creator = value;

        return this;
    }
}
