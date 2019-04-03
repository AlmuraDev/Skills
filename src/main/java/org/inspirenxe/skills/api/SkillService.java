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

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.api.skill.holder.SkillHolderContainer;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface SkillService {

  String UNKNOWN_NAME = "Unknown";

  DecimalFormat getXPFormat();

  /**
   * Returns the passed in {@link SkillHolderContainer} or the container this skill states it inherits from.
   *
   * <Note>
   *   It is expected to only return {@link Optional#empty()} if the parent container is not loaded currently.
   * </Note>
   *
   * @param container The container
   * @return The parent container
   */
  Optional<SkillHolderContainer> getParentContainer(final SkillHolderContainer container);

  Map<UUID, SkillHolderContainer> getContainers();

  default Optional<SkillHolderContainer> getContainer(final UUID containerId) {
    checkNotNull(containerId);

    return Optional.ofNullable(this.getContainers().get(containerId));
  }

  default SkillHolderContainer createContainer(final UUID containerId) {
    return this.createContainer(containerId, UNKNOWN_NAME);
  }

  SkillHolderContainer createContainer(final UUID containerId, final String name);

  void saveContainer(final UUID containerId);

  Optional<SkillHolderContainer> removeContainer(final UUID containerId);
}
