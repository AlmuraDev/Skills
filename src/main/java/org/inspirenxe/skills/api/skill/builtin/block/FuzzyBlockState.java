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
package org.inspirenxe.skills.api.skill.builtin.block;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.api.skill.builtin.FuzzyMatchable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.ArrayList;
import java.util.List;

public final class FuzzyBlockState implements FuzzyMatchable<BlockSnapshot> {

    private final BlockType type;
    private final TraitValue[] traits;

    private FuzzyBlockState(final BlockType type, final TraitValue... traits) {
        this.type = type;
        this.traits = traits;
    }

    public static FuzzyBlockState type(final BlockType type) {
        checkNotNull(type);

        return new FuzzyBlockState(type);
    }

    public static FuzzyBlockState state(final BlockState state) {
        checkNotNull(state);

        final List<TraitValue> traits = new ArrayList<>();

        for (final BlockTrait<?> trait : state.getTraits()) {
            final Object value = state.getTraitValue(trait);

            traits.add(TraitValue.trait(trait, value));
        }

        return new FuzzyBlockState(state.getType(), traits.toArray(new TraitValue[]{}));
    }

    public static FuzzyBlockState state(final BlockType type, final TraitValue... traits) {
        checkNotNull(type);
        checkNotNull(traits);

        return new FuzzyBlockState(type, traits);
    }

    @Override
    public boolean matches(final BlockSnapshot snapshot) {
        if (this.type != snapshot.getState().getType()) {
            return false;
        }

        boolean matches = true;
        for (TraitValue value : this.traits) {
            final Comparable<?> actualValue = snapshot.getState().getTraitValue(value.getTrait()).orElse(null);
            if (actualValue == null) {
                continue;
            }

            if (!hackyEqualsCheck(actualValue, value.getValue())) {
                matches = false;
                break;
            }
        }

        return matches;
    }

    private boolean hackyEqualsCheck(final Object a, final Object b) {
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
