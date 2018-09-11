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

import org.inspirenxe.skills.api.result.experience.ExperienceResult;

public interface Skill {

  SkillType getSkillType();

  SkillHolder getHolder();

  double getCurrentExperience();

  ExperienceResult setExperience(final double experience);

  ExperienceResult addExperience(final double experience);

  default int getCurrentLevel() {
    return this.getSkillType().getLevelFunction().getLevelFor(this.getCurrentExperience());
  }

  /**
   * Returns if this {@link Skill} is initialized. Initialization is defined by the implementation of the skill.
   *
   * @return True if initialized, false if not
   */
  boolean isInitialized();

  /**
   * Returns if this {@link Skill} has been marked dirty. How this is used is completely up to the implementation.
   *
   * @return True if dirty, false if not
   */
  boolean isDirty();

  /**
   * Sets the dirty state of the {@link Skill}. See {@link Skill#isDirty()}.
   *
   * @param dirty True if dirty, false if not
   */
  void setDirty(final boolean dirty);
}
