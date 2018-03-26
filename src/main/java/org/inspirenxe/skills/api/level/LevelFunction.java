/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE <https://github.com/InspireNXE/>
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
package org.inspirenxe.skills.api.level;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.util.annotation.CatalogedBy;

import java.util.function.Function;

/**
 * Simple {@link Function} meant to process an int-based level and return a double representing total experience at that level.
 */
@CatalogedBy(LevelFunctions.class)
public interface LevelFunction extends Function<Integer, Double>, CatalogType {

    double getXPFor(int level);

    int getLevelFor(double xp);

    default double getXPBetween(int lower, int upper) {
        return this.apply(upper) - this.apply(lower);
    }

    default Double apply(Integer value) {
        return this.getXPFor(value);
    }

    /**
     * Instructs the function to build an internal cache of the level table. It is up to the implementation to determine what
     * this method does, if anything. Even if the implementation should make use of a cache, the suggested max is not guaranteed
     * to be honored.
     *
     * @param suggestedMax A suggested maximum level to cache to
     */
    void buildLevelTable(int suggestedMax);
}
