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
import java.util.UUID;

/**
 * Which blocks a block rule applies to, by who placed them. A block a rule does not apply to is neither rewarded nor level gated by
 * it: level gates protect natural resources, not players' own builds.
 */
public enum Creator {
    /** Every block. */
    ANY,
    /** Only blocks no player placed (natural blocks). */
    NATURAL,
    /** Only blocks a player placed. */
    PLACED,
    /** Only blocks the player placed themselves. */
    OWN,
    /** Natural blocks and blocks the player placed themselves. */
    OWN_OR_NATURAL;

    public boolean test(final UUID player, final UUID creator) {
        return switch (this) {
            case ANY -> true;
            case NATURAL -> creator == null;
            case PLACED -> creator != null;
            case OWN -> player.equals(creator);
            case OWN_OR_NATURAL -> creator == null || player.equals(creator);
        };
    }

    public static Optional<Creator> fromConfig(final String name) {
        final String normalized = name.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        for (final Creator creator : Creator.values()) {
            if (creator.name().equals(normalized)) {
                return Optional.of(creator);
            }
        }
        return Optional.empty();
    }
}
