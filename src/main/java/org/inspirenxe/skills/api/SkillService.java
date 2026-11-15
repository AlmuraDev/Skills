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

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface SkillService {

    String UNKNOWN_NAME = "Unknown";

    /**
     * @return A formatter for experience values (one per thread, DecimalFormat is not thread-safe)
     */
    DecimalFormat xpFormat();

    /**
     * @return Every loaded skill type, sorted by name
     */
    Collection<SkillType> skillTypes();

    /**
     * @param id The key ({@code skills:mining}) or the bare name ({@code mining})
     */
    Optional<SkillType> skillType(String id);

    /**
     * Returns the passed in {@link SkillHolderContainer} or the container it shares skills from (e.g. the Nether shares from the
     * Overworld). Only empty if that parent container is not loaded currently.
     */
    Optional<SkillHolderContainer> parentContainer(SkillHolderContainer container);

    Map<ResourceKey, SkillHolderContainer> containers();

    default Optional<SkillHolderContainer> container(final ResourceKey worldKey) {
        Objects.requireNonNull(worldKey, "worldKey");
        return Optional.ofNullable(this.containers().get(worldKey));
    }

    /**
     * @return The container holding the skills used in the given world (its own or the one it shares from)
     */
    default Optional<SkillHolderContainer> containerFor(final ResourceKey worldKey) {
        return this.container(worldKey).flatMap(this::parentContainer);
    }

    SkillHolderContainer createContainer(ResourceKey worldKey);

    void saveContainer(ResourceKey worldKey);

    Optional<SkillHolderContainer> removeContainer(ResourceKey worldKey);
}
