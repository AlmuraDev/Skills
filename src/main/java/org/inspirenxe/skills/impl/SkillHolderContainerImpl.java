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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillHolderContainer;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.SkillType;
import org.spongepowered.api.GameRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SkillHolderContainerImpl implements SkillHolderContainer {

  private final GameRegistry registry;
  private final SkillHolderImpl.Factory factory;
  private final UUID uniqueId;
  private final String name;

  private final Map<UUID, SkillHolder> holders = new HashMap<>();

  @Inject
  public SkillHolderContainerImpl(final GameRegistry registry, final SkillHolderImpl.Factory factory, @Assisted final UUID uniqueId,
    @Assisted final String name) {
    this.registry = registry;
    this.factory = factory;
    this.uniqueId = checkNotNull(uniqueId);
    this.name = checkNotNull(name);
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
  public Map<UUID, SkillHolder> getHolders() {
    return Collections.unmodifiableMap(this.holders);
  }

  @Override
  public SkillHolder createHolder(final UUID holderId, final String name) {
    checkNotNull(holderId);
    checkNotNull(name);

    final SkillHolderImpl holder = this.factory.create(this, holderId, name);
    this.holders.put(holderId, holder);

    final Collection<SkillType> skillTypes = this.registry.getAllOf(SkillType.class);
    skillTypes.forEach(holder::createSkill);

    return holder;
  }

  @Override
  public Optional<SkillHolder> removeHolder(final UUID holderId) {
    checkNotNull(holderId);

    return Optional.ofNullable(this.holders.remove(holderId));
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SkillHolderContainerImpl that = (SkillHolderContainerImpl) o;
    return Objects.equals(this.uniqueId, that.uniqueId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.uniqueId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("id", this.uniqueId)
      .add("name", this.name)
      .toString();
  }

  interface Factory {
    SkillHolderContainerImpl create(final SkillService service, final UUID uniqueId, final String name);
  }
}
