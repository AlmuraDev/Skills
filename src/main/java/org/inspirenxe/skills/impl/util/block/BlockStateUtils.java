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
package org.inspirenxe.skills.impl.util.block;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Map;

public final class BlockStateUtils {

    public static boolean propertiesMatch(BlockState a, BlockState b) {
        boolean propertiesMatch = true;

        final Map<BlockTrait<?>, ?> bTraits = b.getTraitMap();
        for (final Map.Entry<BlockTrait<?>, ?> entry : a.getTraitMap().entrySet()) {
            final BlockTrait<?> trait = entry.getKey();
            final Object aValue = entry.getValue();

            final Object bValue = bTraits.get(trait);

            if (bValue == null) {
                propertiesMatch = false;
                break;
            }

            if (!aValue.equals(bValue)) {
                propertiesMatch = false;
                break;
            }
        }

        return propertiesMatch;
    }
}
