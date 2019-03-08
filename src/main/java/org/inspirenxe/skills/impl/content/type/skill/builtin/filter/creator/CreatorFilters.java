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
package org.inspirenxe.skills.impl.content.type.skill.builtin.filter.creator;

import org.spongepowered.api.entity.living.player.User;

import java.util.UUID;

public final class CreatorFilters {

    public static CreatorFilter CREATOR_ONLY = (cause, skill, snapshot, flags) -> {
        final UUID creator = snapshot.getCreator().orElse(null);
        if (creator == null) {
            return false;
        }

        final User user = cause.first(User.class).orElse(null);
        if (user == null) {
            return false;
        }

        // Someone is breaking someone else's block, don't reward
        return user.getUniqueId().equals(creator);
    };

    public static CreatorFilter CREATOR_OR_NATURAL = (cause, skill, snapshot, flags) -> {
        final UUID creator = snapshot.getCreator().orElse(null);
        if (creator == null) {
            return true;
        }

        final User user = cause.first(User.class).orElse(null);
        if (user == null) {
            return false;
        }

        // Someone is breaking someone else's block, don't reward
        return user.getUniqueId().equals(creator);
    };

    public static CreatorFilter CREATOR_BUT_TRACKED_OR_NATURAL = (cause, skill, snapshot, flags) -> {
        // Is it natural?
        final UUID creator = snapshot.getCreator().orElse(null);
        if (creator == null) {
            return true;
        }

        // Something broke someone's block, don't reward
        final User user = cause.first(User.class).orElse(null);
        if (user == null) {
            return false;
        }

        // Someone is breaking someone else's block, don't reward
        if (!user.getUniqueId().equals(creator)) {
            return false;
        }

        return !flags.isEmpty();
    };

    public static CreatorFilter NATURAL = (cause, skill, snapshot, flags) -> !snapshot.getCreator().isPresent();

    public static CreatorFilter NOT_NATURAL = (cause, skill, snapshot, flags) -> snapshot.getCreator().isPresent();

    public static CreatorFilter ANY = (cause, skill, snapshot, flags) -> true;
}
