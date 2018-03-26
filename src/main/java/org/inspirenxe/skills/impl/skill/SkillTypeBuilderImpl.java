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

import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.level.LevelFunction;
import org.inspirenxe.skills.impl.level.UnknownLevelFunction;

public final class SkillTypeBuilderImpl implements SkillType.Builder {

  private int minLevel, maxLevel;
  private LevelFunction levelFunction = UnknownLevelFunction.instance;

  @Override
  public SkillType.Builder minLevel(int level) {
    this.minLevel = level;
    return this;
  }

  int minLevel() {
    return this.minLevel;
  }

  @Override
  public SkillType.Builder maxLevel(int level) {
    this.maxLevel = level;
    return this;
  }

  int maxLevel() {
    return this.maxLevel;
  }

  @Override
  public SkillType.Builder levelFunction(LevelFunction function) {
    this.levelFunction = function;
    return this;
  }

  LevelFunction levelFunction() {
    return this.levelFunction;
  }

  @Override
  public SkillType.Builder from(SkillType value) {
    this.minLevel = value.getMinLevel();
    this.maxLevel = value.getMaxLevel();
    this.levelFunction = value.getLevelFunction();
    return this;
  }

  @Override
  public SkillType.Builder reset() {
    this.minLevel = 0;
    this.maxLevel = 0;
    this.levelFunction = UnknownLevelFunction.instance;
    return this;
  }

  @Override
  public SkillType build(String id, String name) {
    checkNotNull(id);
    checkNotNull(name);
    checkNotNull(this.levelFunction);

    return new SkillTypeImpl(id, name, this);
  }
}
