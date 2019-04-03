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
package org.inspirenxe.skills.api.skill.builtin.query.block.state;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;

public final class FuzzyBlockState {

    public static FuzzyBlockState state(BlockType type, TraitValue... traits) {
        checkNotNull(type);
        checkNotNull(traits);

        return new FuzzyBlockState(type, traits);
    }

    private final BlockType type;
    private final TraitValue[] traits;

    private FuzzyBlockState(final BlockType type, final TraitValue... traits) {
        this.type = type;
        this.traits = traits;
    }

    public boolean matches(final BlockState state) {
        if (this.type != state.getType()) {
            return false;
        }

        boolean matches = false;
        for (TraitValue value : this.traits) {
            final Comparable<?> actualValue = state.getTraitValue(value.getTrait()).orElse(null);
            if (actualValue == null) {
                continue;
            }

            if (hackyEqualsCheck(actualValue, value.getValue())) {
                matches = true;
                break;
            }
        }

        return matches;
    }

    private boolean hackyEqualsCheck(Object a, Object b) {
        if (a instanceof String) {
            return a.toString().equalsIgnoreCase(b.toString());
        }

        if (a instanceof Enum) {
            return ((Enum) a).name().equalsIgnoreCase(b.toString());
        }

        if (a instanceof Number) {
            return a == b;
        }

        return a.equals(b);
    }
}
