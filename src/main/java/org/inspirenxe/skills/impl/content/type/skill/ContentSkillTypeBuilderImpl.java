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
package org.inspirenxe.skills.impl.content.type.skill;

import com.almuradev.droplet.content.type.AbstractContentBuilder;
import com.almuradev.droplet.registry.reference.RegistryReference;
import org.inspirenxe.skills.api.function.level.LevelFunction;
import org.inspirenxe.skills.impl.skill.SkillTypeImpl;

public final class ContentSkillTypeBuilderImpl extends AbstractContentBuilder<SkillTypeImpl> implements ContentSkillTypeBuilder {

  private RegistryReference<LevelFunction> levelFunction;
  private int minLevel, maxLevel;

  @Override
  public void levelFunction(final RegistryReference<LevelFunction> levelFunction) {
    this.levelFunction = levelFunction;
  }

  @Override
  public void minLevel(final int minLevel) {
    this.minLevel = minLevel;
  }

  @Override
  public void maxLevel(final int maxLevel) {
    this.maxLevel = maxLevel;
  }

  @Override
  public SkillTypeImpl build() {
    return new SkillTypeImpl(this.key(), this.levelFunction, this.minLevel, this.maxLevel);
  }
}
