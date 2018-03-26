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

import com.google.common.base.MoreObjects;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.level.LevelFunction;

import java.util.Objects;

public class SkillTypeImpl implements SkillType {

  private final String id, name;
  private final int minlevel, maxLevel;
  private final LevelFunction levelFunction;

  protected SkillTypeImpl(String id, String name, SkillTypeBuilderImpl builder) {
    this.id = id;
    this.name = name;
    this.minlevel = builder.minLevel();
    this.maxLevel = builder.maxLevel();
    this.levelFunction = builder.levelFunction();
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public int getMinLevel() {
    return this.minlevel;
  }

  @Override
  public int getMaxLevel() {
    return this.maxLevel;
  }

  @Override
  public LevelFunction getLevelFunction() {
    return this.levelFunction;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SkillTypeImpl)) {
      return false;
    }
    final SkillTypeImpl skillType = (SkillTypeImpl) o;
    return Objects.equals(id, skillType.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", this.id)
        .add("name", this.name)
        .add("minLevel", this.minlevel)
        .add("maxLevel", this.maxLevel)
        .add("levelFunction", this.levelFunction)
        .toString();
  }
}
