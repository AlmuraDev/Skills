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

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;

import java.util.List;

/**
 * One rule of a skill: what it matches, the level it needs and what it pays.
 *
 * @param level The level needed, or null for none
 * @param xp The experience paid, or null for none
 * @param money The money modifier passed to the skill's economy formula, or null for no money
 * @param deny The verb of the message shown when the level gate denies (e.g. "break"), or null for no message
 */
public record Rule(Trigger trigger, List<Matcher<?>> matchers, Integer level, Double xp, Double money, Creator creator, String deny) {

    boolean couldMatchBlock(final BlockType type) {
        for (final Matcher<?> matcher : this.matchers) {
            if (matcher instanceof Matcher.Block block && block.couldMatch(type)) {
                return true;
            }
        }
        return false;
    }

    boolean couldMatchItem(final ItemType type) {
        for (final Matcher<?> matcher : this.matchers) {
            if (matcher instanceof Matcher.Item item && item.couldMatch(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean matches(final BlockState state) {
        for (final Matcher<?> matcher : this.matchers) {
            if (matcher instanceof Matcher.Block block && block.matches(state)) {
                return true;
            }
        }
        return false;
    }

    public boolean matches(final ItemType type) {
        for (final Matcher<?> matcher : this.matchers) {
            if (matcher instanceof Matcher.Item item && item.matches(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean rewards() {
        return this.xp != null || this.money != null;
    }
}
