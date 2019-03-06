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
package org.inspirenxe.skills.impl.event;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

public enum BlockCreationFlags {
    SAPLING((cause, context) -> {
        final BlockSnapshot snapshot = cause.first(BlockSnapshot.class).orElse(null);
        if (snapshot == null || snapshot.getState().getType() != BlockTypes.SAPLING) {
            return false;
        }
        final User user = cause.first(User.class).orElse(null);
        return user != null;
    }),

    BONEMEAL((cause, context) -> {
        final ItemStackSnapshot snapshot = context.get(EventContextKeys.USED_ITEM).orElse(null);
        if (snapshot == null) {
            return false;
        }

        final Value<DyeColor> dyeColor = snapshot.getValue(Keys.DYE_COLOR).orElse(null);
        if (dyeColor == null) {
            return false;
        }
        return snapshot.getType() == ItemTypes.DYE && dyeColor.get() == DyeColors.WHITE;
    });

    private final BiPredicate<Cause, EventContext> matcher;

    BlockCreationFlags(final BiPredicate<Cause, EventContext> matcher) {
        this.matcher = matcher;
    }

    public static Set<BlockCreationFlags> getFlags(final Cause cause, final EventContext context) {
        final Set<BlockCreationFlags> flags = new HashSet<>();

        for (BlockCreationFlags flag : BlockCreationFlags.values()) {
            if (flag.matcher.test(cause, context)) {
                flags.add(flag);
            }
        }

        return flags;
    }

    public static Set<BlockCreationFlags> unmask(final long mask) {
        final Set<BlockCreationFlags> flags = new HashSet<>();

        for (BlockCreationFlags flag : BlockCreationFlags.values()) {
            if ((mask & (1 << flag.ordinal())) != 0) {
                flags.add(flag);
            }
        }

        return flags;
    }

    public static long mask(final Set<BlockCreationFlags> flags) {
        long mask = 0;

        for (final BlockCreationFlags flag : flags) {
            mask |= 1 << flag.ordinal();
        }

        return mask;
    }
}
