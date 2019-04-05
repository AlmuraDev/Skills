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
package org.inspirenxe.skills.api.skill.builtin.filter.block;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import org.inspirenxe.skills.api.skill.builtin.block.FuzzyBlockState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class BlockFilters {

    private static final BlockFuzzyFilter ALL_TYPES;

    static {
        final Set<FuzzyBlockState> states = new HashSet<>();
        for (BlockType type : Sponge.getRegistry().getAllOf(BlockType.class)) {
            states.add(FuzzyBlockState.type(type));
        }

        ALL_TYPES = new BlockFuzzyFilter(states);
    }

    private BlockFilters() {
    }

    public static BlockFuzzyFilter block(final BlockType... value) {
        checkNotNull(value);

        final Set<FuzzyBlockState> states = new HashSet<>();
        for (BlockType type : value) {
            states.add(FuzzyBlockState.type(type));
        }

        return new BlockFuzzyFilter(states);
    }

    public static BlockFuzzyFilter block(final String... value) {
        checkNotNull(value);

        final Set<FuzzyBlockState> states = new HashSet<>();
        for (String id : value) {
            final BlockType type = Sponge.getRegistry().getType(BlockType.class, id).orElse(null);
            if (type == null) {
                // TODO Message
            } else {
                states.add(FuzzyBlockState.type(type));
            }
        }
        return new BlockFuzzyFilter(states);
    }

    public static BlockFuzzyFilter blockTypesFor(final String... value) {
        checkNotNull(value);

        final Set<FuzzyBlockState> states = new HashSet<>();
        for (String id : value) {
            final Collection<BlockType> types = Sponge.getRegistry().getAllFor(id, BlockType.class);
            if (types.isEmpty()) {
                // TODO Message
            } else {
                types.forEach(type -> states.add(FuzzyBlockState.type(type)));
            }
        }

        return new BlockFuzzyFilter(states);
    }

    public static BlockFuzzyFilter block() {
        return ALL_TYPES;
    }

    public static BlockFuzzyFilter state(final FuzzyBlockState... value) {
        return new BlockFuzzyFilter(Sets.newHashSet(checkNotNull(value)));
    }

    public static BlockFuzzyFilter state(final String... value) {
        checkNotNull(value);

        final Set<FuzzyBlockState> states = new HashSet<>();
        for (String id : value) {
            final BlockState type = Sponge.getRegistry().getType(BlockState.class, id).orElse(null);
            if (type == null) {
                // TODO Message
            } else {
                states.add(FuzzyBlockState.state(type));
            }
        }
        return new BlockFuzzyFilter(states);
    }

    public static BlockFuzzyFilter stateFor(final String... value) {
        checkNotNull(value);

        final Set<FuzzyBlockState> states = new HashSet<>();
        for (String id : value) {
            final Collection<BlockState> types = Sponge.getRegistry().getAllFor(id, BlockState.class);
            if (types.isEmpty()) {
                // TODO Message
            } else {
                types.forEach(type -> states.add(FuzzyBlockState.state(type)));
            }
        }

        return new BlockFuzzyFilter(states);
    }
}
