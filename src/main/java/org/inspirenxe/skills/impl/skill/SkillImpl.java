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
package org.inspirenxe.skills.impl.skill;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.inspirenxe.skills.impl.event.experience.change.ChangeExperiencePostEventImpl;
import org.inspirenxe.skills.impl.event.experience.change.ChangeExperiencePreEventImpl;
import org.spongepowered.api.event.EventManager;

import java.util.Objects;

public final class SkillImpl implements Skill {

  private final EventManager eventManager;
  private final SkillType skillType;
  private final SkillHolder skillHolder;
  private double experience;
  private boolean dirtyState, isInitialized;

  @Inject
  private SkillImpl(final EventManager eventManager, @Assisted final SkillType skillType, @Assisted final SkillHolder skillHolder) {
    checkNotNull(skillType);
    checkNotNull(skillHolder);
    this.eventManager = eventManager;
    this.skillType = skillType;
    this.skillHolder = skillHolder;
  }

  @Override
  public SkillType getSkillType() {
    return this.skillType;
  }

  @Override
  public SkillHolder getHolder() {
    return this.skillHolder;
  }

  @Override
  public double getCurrentExperience() {
    return this.experience;
  }

  @Override
  public Skill setExperience(final double experience) {
    checkState(experience >= 0, "Setting experience must be greater than 0!");

    if (!this.isInitialized) {
      this.isInitialized = true;
    }

    final double originalExperience = this.experience;
    final ExperienceEvent.Change.Pre event = new ChangeExperiencePreEventImpl(this, originalExperience, experience);
    if (this.eventManager.post(event)) {
      return this;
    }

    this.experience = event.getExperience();

    this.eventManager.post(new ChangeExperiencePostEventImpl(this, originalExperience, this.experience));
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
  public void setDirtyState(final boolean dirtyState) {
    this.dirtyState = dirtyState;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SkillImpl)) {
      return false;
    }
    final SkillImpl skill = (SkillImpl) o;
    return Objects.equals(this.skillHolder, skill.skillHolder) && Objects.equals(this.skillType, skill.skillType);

  }

  @Override
  public int hashCode() {
    return Objects.hash(this.skillHolder, this.skillType);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("type", this.skillType)
        .add("containerUniqueId", this.skillHolder.getContainerUniqueId())
        .add("holderUniqueId", this.skillHolder.getHolderUniqueId())
        .toString();
  }

  public interface Factory {
    SkillImpl create(final SkillType skillType, final SkillHolder skillHolder);
  }
}
