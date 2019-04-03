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
package org.inspirenxe.skills.api.skill.builtin.query.block;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import org.inspirenxe.skills.api.skill.builtin.query.block.state.FuzzyBlockState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class BlockQueries {

    public static MatchTypeBlockQuery blockType(final String... blockIds) {
        checkNotNull(blockIds);

        final Set<BlockType> types = new HashSet<>();
        for (String id : blockIds) {
            final BlockType blockType = Sponge.getRegistry().getType(BlockType.class, id).orElse(null);
            if (blockType == null) {
                // TODO Message
            } else {
                types.add(blockType);
            }
        }

        return new MatchTypeBlockQuery(Sets.newHashSet(types));
    }

    public static BlockQuery blockType(final BlockType... types) {
        checkNotNull(types);

        return new MatchTypeBlockQuery(Sets.newHashSet(types));
    }

    public static BlockQuery exactState(final BlockState... states) {
        checkNotNull(states);

        return new MatchStateBlockQuery(Sets.newHashSet(states));
    }

    public static BlockQuery blockTypesFor(final String... ids) {
        checkNotNull(ids);

        final Set<BlockType> types = new HashSet<>();
        for (String id : ids) {
            final Collection<BlockType> found = Sponge.getRegistry().getAllFor(id, BlockType.class);
            if (found.isEmpty()) {
                // TODO Message
            } else {
                types.addAll(found);
            }
        }

        return new MatchTypeBlockQuery(types);
    }

    public static BlockQuery blockTypes() {
        return new MatchTypeBlockQuery(Sets.newHashSet(Sponge.getRegistry().getAllOf(BlockType.class)));
    }

    public static BlockQuery fuzzyState(final FuzzyBlockState... states) {
        checkNotNull(states);

        return new FuzzyMatchStateBlockQuery(Sets.newHashSet(states));
    }

    private BlockQueries() {}
}
