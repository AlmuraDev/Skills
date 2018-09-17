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
package org.inspirenxe.skills.api.event;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillType;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.util.annotation.eventgen.AbsoluteSortPosition;

import java.util.UUID;

public interface ExperienceEvent extends Event {

  /**
   * The {@link UUID} which is unique per container.
   *
   * @return The unique id
   */
  @AbsoluteSortPosition(1)
  UUID getContainerId();

  /**
   * The {@link UUID} which is unique per holder.
   *
   * @return The unique id
   */
  @AbsoluteSortPosition(2)
  UUID getHolderId();

  /**
   * Gets the {@link SkillType}.
   *
   * @return The skill type
   */
  @AbsoluteSortPosition(3)
  SkillType getSkillType();

  /**
   * Gets the experience that will be changed on the {@link Skill}.
   *
   * @return The experience change
   */
  double getExperience();
}
