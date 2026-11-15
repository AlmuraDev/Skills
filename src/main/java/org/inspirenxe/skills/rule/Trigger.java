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
package org.inspirenxe.skills.rule;

import java.util.Locale;
import java.util.Optional;

/**
 * What a rule reacts to.
 */
public enum Trigger {
    /** Breaking a block (matched against the block before it broke). */
    BREAK(true),
    /** Placing a block, e.g. planting a crop or hoeing farmland (matched against the new block). */
    PLACE(true),
    /** Right clicking a block. A level gate cancels the interaction. */
    INTERACT_BLOCK(true),
    /** Using a held item: right clicking with it, or hitting a block with it. A level gate denies the use. */
    USE_TOOL(false),
    /** Crafting an item. A level gate cancels the craft. */
    CRAFT(false),
    /** Items dropped by a broken block, e.g. harvested crops. A level gate removes that drop. */
    DROP(false),
    /** An item finishing smelting, paid to the player who put the input in. A level here only withholds the XP. */
    SMELT(false);

    private final boolean blocks;

    Trigger(final boolean blocks) {
        this.blocks = blocks;
    }

    /**
     * @return True if rules of this trigger match blocks, false if they match items
     */
    public boolean matchesBlocks() {
        return this.blocks;
    }

    public String configName() {
        return this.name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public static Optional<Trigger> fromConfig(final String name) {
        final String normalized = name.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        for (final Trigger trigger : Trigger.values()) {
            if (trigger.name().equals(normalized)) {
                return Optional.of(trigger);
            }
        }
        return Optional.empty();
    }
}
