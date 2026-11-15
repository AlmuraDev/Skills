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
package org.inspirenxe.skills.core;

import org.apache.logging.log4j.Logger;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillHolderContainer;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.database.Database;
import org.spongepowered.api.ResourceKey;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class SkillHolderContainerImpl implements SkillHolderContainer {

    private final Logger logger;
    private final Database database;
    private final Supplier<Collection<SkillType>> skillTypes;
    private final ResourceKey key;
    private final Map<UUID, SkillHolder> holders = new ConcurrentHashMap<>();

    SkillHolderContainerImpl(final Logger logger, final Database database, final Supplier<Collection<SkillType>> skillTypes,
            final ResourceKey key) {
        this.logger = logger;
        this.database = database;
        this.skillTypes = skillTypes;
        this.key = key;
    }

    @Override
    public ResourceKey key() {
        return this.key;
    }

    @Override
    public Map<UUID, SkillHolder> holders() {
        return Collections.unmodifiableMap(this.holders);
    }

    @Override
    public SkillHolder createHolder(final UUID holderId, final String name) {
        Objects.requireNonNull(holderId, "holderId");
        Objects.requireNonNull(name, "name");
        // Never replace a live holder, it may hold unsaved experience
        final SkillHolder existing = this.holders.get(holderId);
        if (existing != null) {
            return existing;
        }
        final SkillHolderImpl holder = new SkillHolderImpl(this, holderId, name);
        for (final SkillType type : this.skillTypes.get()) {
            holder.addSkill(new SkillImpl(this.logger, this.database, holder, type));
        }
        this.holders.put(holderId, holder);
        return holder;
    }

    @Override
    public Optional<SkillHolder> removeHolder(final UUID holderId) {
        Objects.requireNonNull(holderId, "holderId");
        return Optional.ofNullable(this.holders.remove(holderId));
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof SkillHolderContainerImpl other && this.key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return "SkillHolderContainer{" + this.key + "}";
    }
}
