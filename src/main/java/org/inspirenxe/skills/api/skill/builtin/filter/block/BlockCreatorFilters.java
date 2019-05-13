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

import org.inspirenxe.skills.api.event.BlockCreationFlags;
import org.inspirenxe.skills.api.skill.builtin.BlockCreationTracker;
import org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys;
import org.spongepowered.api.block.BlockSnapshot;

import java.util.Set;
import java.util.UUID;

public final class BlockCreatorFilters {

    private static final BlockCreatorFilter CREATOR, CREATOR_TRACKED, NATURAL;

    static {
        CREATOR = query -> {
            final BlockSnapshot snapshot = query.getContext().get(SkillsEventContextKeys.PROCESSING_BLOCK).orElse(BlockSnapshot.NONE);
            final UUID creator = snapshot.getCreator().orElse(null);
            if (creator == null) {
                return false;
            }

            return query.getUser().getUniqueId().equals(creator);
        };

        CREATOR_TRACKED = query -> {
            final BlockSnapshot snapshot = query.getContext().get(SkillsEventContextKeys.PROCESSING_BLOCK).orElse(BlockSnapshot.NONE);
            final UUID creator = snapshot.getCreator().orElse(null);
            if (creator == null) {
                return true;
            }

            if (!query.getUser().getUniqueId().equals(creator)) {
                return false;
            }

            final BlockCreationTracker tracker = query.getContext().get(SkillsEventContextKeys.BLOCK_CREATION_TRACKER).orElse(null);
            final Set<BlockCreationFlags> flags = tracker.getCreationFlags(snapshot);
            return !flags.isEmpty();
        };

        NATURAL = query -> {
            final BlockSnapshot snapshot = query.getContext().get(SkillsEventContextKeys.PROCESSING_BLOCK).orElse(BlockSnapshot.NONE);
            return !snapshot.getCreator().isPresent();
        };
    }

    private BlockCreatorFilters() {
    }

    public static BlockCreatorFilter creator() {
        return CREATOR;
    }

    public static BlockCreatorFilter creatorTracked() {
        return CREATOR_TRACKED;
    }

    public static BlockCreatorFilter natural() {
        return NATURAL;
    }
}
