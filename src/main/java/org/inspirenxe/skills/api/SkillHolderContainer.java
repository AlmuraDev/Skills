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
package org.inspirenxe.skills.api;

import org.spongepowered.api.ResourceKey;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * The skills of every holder (player) in one world, or in a group of worlds sharing their skills.
 */
public interface SkillHolderContainer {

    /**
     * @return The key of the world this container belongs to
     */
    ResourceKey key();

    Map<UUID, SkillHolder> holders();

    default Optional<SkillHolder> holder(final UUID holderId) {
        Objects.requireNonNull(holderId, "holderId");
        return Optional.ofNullable(this.holders().get(holderId));
    }

    /**
     * Creates the holder with a skill of every loaded type, or returns the live holder if there is one already.
     */
    SkillHolder createHolder(UUID holderId, String name);

    default void saveHolder(final UUID holderId) {
        this.holder(holderId).ifPresent(SkillHolder::save);
    }

    Optional<SkillHolder> removeHolder(UUID holderId);

    default void save() {
        this.holders().values().forEach(SkillHolder::save);
    }
}
