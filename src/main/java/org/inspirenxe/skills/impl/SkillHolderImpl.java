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
package org.inspirenxe.skills.impl;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SkillHolderImpl implements SkillHolder {

  private final SkillImpl.Factory factory;
  private final UUID containerUniqueId, holderUniqueId;
  private final Map<SkillType, Skill> skills = new HashMap<>();

  @Inject
  private SkillHolderImpl(final SkillImpl.Factory factory, @Assisted("container") UUID containerUniqueId, @Assisted("holder") UUID holderUniqueId) {
    this.factory = factory;
    this.containerUniqueId = containerUniqueId;
    this.holderUniqueId = holderUniqueId;
  }

  @Override
  public UUID getContainerUniqueId() {
    return this.containerUniqueId;
  }

  @Override
  public UUID getHolderUniqueId() {
    return this.holderUniqueId;
  }

  @Override
  public Optional<Skill> getSkill(SkillType type) {
    return Optional.ofNullable(this.skills.get(type));
  }

  @Override
  public Map<SkillType, Skill> getSkills() {
    return Collections.unmodifiableMap(this.skills);
  }

  @Override
  public Skill addSkill(SkillType type) {
    final Skill skill = this.factory.create(type, this);

    this.skills.put(type, skill);

    return skill;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SkillHolderImpl)) {
      return false;
    }
    final SkillHolderImpl that = (SkillHolderImpl) o;
    return Objects.equals(this.containerUniqueId, that.containerUniqueId) &&
        Objects.equals(this.holderUniqueId, that.holderUniqueId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.containerUniqueId, this.holderUniqueId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("containerUniqueId", this.containerUniqueId)
        .add("holder", this.holderUniqueId)
        .add("skills", this.skills)
        .toString();
  }

  public interface Factory {
    SkillHolderImpl create(@Assisted("container") UUID containerUniqueId, @Assisted("holder") UUID holderUniqueId);
  }
}
