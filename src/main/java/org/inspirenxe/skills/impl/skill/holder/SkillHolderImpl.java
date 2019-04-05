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
package org.inspirenxe.skills.impl.skill.holder;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.SkillType;
import org.inspirenxe.skills.api.skill.holder.SkillHolder;
import org.inspirenxe.skills.api.skill.holder.SkillHolderContainer;
import org.inspirenxe.skills.impl.skill.SkillImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SkillHolderImpl implements SkillHolder {

    private final SkillImpl.Factory factory;
    private final SkillHolderContainer container;
    private final UUID uniqueId;
    private final String name;

    private final Map<SkillType, Skill> skills = new HashMap<>();

    @Inject
    private SkillHolderImpl(final SkillImpl.Factory factory, @Assisted final SkillHolderContainer container, @Assisted final UUID uniqueId,
        @Assisted final String name) {
        this.factory = factory;
        this.container = checkNotNull(container);
        this.uniqueId = checkNotNull(uniqueId);
        this.name = checkNotNull(name);
    }

    @Override
    public SkillHolderContainer getContainer() {
        return this.container;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Map<SkillType, Skill> getSkills() {
        return Collections.unmodifiableMap(this.skills);
    }

    @Override
    public Skill createSkill(final SkillType type) {
        checkNotNull(type);

        final SkillImpl skill = this.factory.create(this, type);
        this.skills.put(type, skill);
        return skill;
    }

    @Override
    public Optional<Skill> removeSkill(final SkillType type) {
        checkNotNull(type);

        return Optional.ofNullable(this.skills.remove(type));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SkillHolderImpl)) {
            return false;
        }
        final SkillHolderImpl other = (SkillHolderImpl) o;
        return Objects.equals(this.uniqueId, other.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uniqueId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("container", this.container.getUniqueId())
            .add("id", this.uniqueId)
            .add("name", this.name)
            .add("skills", this.skills)
            .toString();
    }

    public interface Factory {

        SkillHolderImpl create(final SkillHolderContainer container, final UUID uniqueId, final String name);
    }
}
