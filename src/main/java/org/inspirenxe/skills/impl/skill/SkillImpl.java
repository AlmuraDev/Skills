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
package org.inspirenxe.skills.impl.skill;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillManager;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.event.experience.change.ChangeExperiencePostEventImpl;
import org.inspirenxe.skills.impl.event.experience.change.ChangeExperiencePreEventImpl;
import org.spongepowered.api.Sponge;

import java.util.Objects;

public final class SkillImpl implements Skill {

    private final SkillType skillType;
    private final SkillHolder skillHolder;
    private double experience;
    private boolean dirtyState, isInitialized;

    private SkillImpl(SkillType skillType, SkillHolder skillHolder) {
        checkNotNull(skillType);
        checkNotNull(skillHolder);

        this.skillType = skillType;
        this.skillHolder = skillHolder;
    }

    public static SkillImpl of(SkillType skillType, SkillHolder skillHolder) {
        return new SkillImpl(skillType, skillHolder);
    }

    @Override
    public final SkillType getSkillType() {
        return this.skillType;
    }

    @Override
    public final SkillHolder getHolder() {
        return this.skillHolder;
    }

    @Override
    public final double getCurrentExperience() {
        return this.experience;
    }

    @Override
    public Skill setExperience(double experience) {
        checkState(experience >= 0, "Setting experience must be greater than 0!");

        if (!this.isInitialized) {
            this.isInitialized = true;
        }

        final double originalExperience = this.experience;
        final ExperienceEvent.Change.Pre event = new ChangeExperiencePreEventImpl(this, originalExperience, experience);
        if (Sponge.getEventManager().post(event)) {
            return this;
        }

        this.experience = event.getExperience();

        Sponge.getEventManager().post(new ChangeExperiencePostEventImpl(this, originalExperience, this.experience));
        return this;
    }

    @Override
    public boolean isInitialized() {
        return this.isInitialized;
    }

    @Override
    public boolean isDirtyState() {
        return this.dirtyState;
    }

    @Override
    public void setDirtyState(boolean dirtyState) {
        this.dirtyState = dirtyState;

        if (this.dirtyState) {
            SkillsImpl.instance.skillManager.queueToSave(this.getHolder());
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SkillImpl)) {
            return false;
        }
        final SkillImpl skill = (SkillImpl) o;
        return Objects.equals(skillType, skill.skillType) &&
                Objects.equals(skillHolder, skill.skillHolder);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(skillType, skillHolder);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this)
                .add("skillType", this.skillType)
                .add("containerUniqueId", this.skillHolder.getContainerUniqueId())
                .add("holderUniqueId", this.skillHolder.getHolderUniqueId())
                .toString();
    }
}
