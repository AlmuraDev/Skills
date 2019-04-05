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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.inspirenxe.skills.api.skill.builtin.query.transaction.BlockSnapshotTransactionQuery;
import org.spongepowered.api.entity.living.player.User;

import java.util.UUID;

public final class BlockCreatorFilters {

    private static final BlockCreatorFilter CREATOR_ONLY, CREATOR_OR_NATURAL, CREATOR_TRACKED_OR_NATURAL, NATURAL;

    static {
        CREATOR_ONLY = new BlockCreatorFilter() {
            @Override
            public boolean queryResponse(@NonNull final BlockSnapshotTransactionQuery query) {
                final UUID creator = query.getTransaction().getOriginal().getCreator().orElse(null);
                if (creator == null) {
                    return false;
                }

                final User user = query.getCause().first(User.class).orElse(null);
                if (user == null) {
                    return false;
                }

                return user.getUniqueId().equals(creator);
            }
        };

        CREATOR_OR_NATURAL = new BlockCreatorFilter() {
            @Override
            public boolean queryResponse(@NonNull final BlockSnapshotTransactionQuery query) {
                final UUID creator = query.getTransaction().getOriginal().getCreator().orElse(null);
                if (creator == null) {
                    return true;
                }

                final User user = query.getCause().first(User.class).orElse(null);
                if (user == null) {
                    return false;
                }

                return user.getUniqueId().equals(creator);
            }
        };

        CREATOR_TRACKED_OR_NATURAL = new BlockCreatorFilter() {
            @Override
            public boolean queryResponse(@NonNull final BlockSnapshotTransactionQuery query) {
                final UUID creator = query.getTransaction().getOriginal().getCreator().orElse(null);
                if (creator == null) {
                    return true;
                }

                final User user = query.getCause().first(User.class).orElse(null);
                if (user == null) {
                    return false;
                }

                if (!user.getUniqueId().equals(creator)) {
                    return false;
                }

                return !query.getCreationFlags().isEmpty();
            }
        };

        NATURAL = new BlockCreatorFilter() {
            @Override
            public boolean queryResponse(@NonNull final BlockSnapshotTransactionQuery query) {
                final UUID creator = query.getTransaction().getOriginal().getCreator().orElse(null);
                return creator == null;
            }
        };
    }

    public static BlockCreatorFilter creatorOnly() {
        return CREATOR_ONLY;
    }

    public static BlockCreatorFilter creatorOrNatural() {
        return CREATOR_OR_NATURAL;
    }

    public static BlockCreatorFilter creatorTrackedOrNatural() {
        return CREATOR_TRACKED_OR_NATURAL;
    }

    public static BlockCreatorFilter natural() {
        return NATURAL;
    }

    private BlockCreatorFilters() {}
}
