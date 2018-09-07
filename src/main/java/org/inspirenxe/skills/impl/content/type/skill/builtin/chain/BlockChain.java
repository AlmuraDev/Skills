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
import org.inspirenxe.skills.impl.content.type.skill.builtin.Chain;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public final class BlockChain extends Chain<BlockChain> {

    public List<BlockState> toQuery = new ArrayList<>();
    public boolean inverseQuery = false, matchOnlyType = false;

    public BlockChain inverseQuery() {
        if (this.inErrorState) {
            return this;
        }
        this.inverseQuery = true;
        return this;
    }

    public BlockChain query(final String id) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(id);
        final BlockType blockType = Sponge.getRegistry().getType(BlockType.class, id).orElse(null);
        if (blockType == null) {
            SkillsImpl.INSTANCE.getLogger().error("Unknown block id '" + id + "' given to block chain!");
            this.inErrorState = true;
            return this;
        }
        this.toQuery.add(blockType.getDefaultState());
        return this;
    }

    public BlockChain query(final BlockType value) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(value);
        this.toQuery.add(value.getDefaultState());
        return this;
    }

    public BlockChain query(final BlockState value) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(value);
        this.toQuery.add(value);
        return this;
    }

    public BlockChain query(final BlockState... values) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(values);
        this.toQuery.addAll(Arrays.asList(values));
        return this;
    }

    public BlockChain matchTypeOnly() {
        if (this.inErrorState) {
            return this;
        }
        this.matchOnlyType = true;
        return this;
    }

    @Override
    public BlockChain from(final BlockChain builder) {
        super.from(builder);

        if (this.inErrorState) {
            return this;
        }

        this.matchOnlyType = builder.matchOnlyType;

        return this;
    }
}
