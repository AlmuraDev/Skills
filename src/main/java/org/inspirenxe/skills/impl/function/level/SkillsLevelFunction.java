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
package org.inspirenxe.skills.impl.function.level;

import static com.google.common.base.Preconditions.checkState;

import com.almuradev.droplet.registry.RegistryKey;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.inspirenxe.skills.api.function.level.LevelFunction;
import org.inspirenxe.skills.impl.function.SkillsCatalogFunction;
import org.slf4j.Logger;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

public final class SkillsLevelFunction implements SkillsCatalogFunction, LevelFunction {

  private final RegistryKey registryKey;
  private final Expression expression;
  private final Logger logger;

  private double[] xpTable = new double[100];

  @Inject
  public SkillsLevelFunction(@Assisted final RegistryKey registryKey, @Assisted final String formula, final Logger logger) {
    this.registryKey = registryKey;
    this.expression = new ExpressionBuilder(formula).variable("L").build();
    this.logger = logger;
    Arrays.fill(this.xpTable, UNKNOWN_EXP);
  }

  @Override
  public String getId() {
    return this.registryKey.toString();
  }

  @Override
  public String getName() {
    return this.registryKey.value();
  }

  @Override
  public double getXPFor(int level) {
    checkState(level > 0, "Level must be positive and higher than zero!");

    if (this.xpTable.length < level) {
      return UNKNOWN_EXP;
    }

    final double cache = this.xpTable[level - 1];

    return cache == UNKNOWN_EXP ? 0 : cache;
  }

  @Override
  public int getLevelFor(double xp) {
    checkState(xp > UNKNOWN_EXP, "XP must be positive!");

    for (int i = this.xpTable.length; i > 0; i--) {
      double cache = this.xpTable[i - 1];

      if (cache == UNKNOWN_EXP) {
        continue;
      }

      if (xp >= cache) {
        return i;
      }
    }

    return UNKNOWN_LEVEL;
  }

  @Override
  public void buildLevelTable(int suggestedMax) {
    this.logger.warn("Printing level table for: {}", this.getId());
    if (suggestedMax > this.xpTable.length) {
      final int length = this.xpTable.length;
      this.xpTable = Arrays.copyOf(this.xpTable, this.xpTable.length * 2);
      Arrays.fill(this.xpTable, length, this.xpTable.length - 1, UNKNOWN_EXP);
    }

    if (this.xpTable[suggestedMax] == UNKNOWN_EXP) {
      for (int lvl = 1; lvl <= suggestedMax; lvl++) {
        this.expression.setVariable("L", lvl);
        this.xpTable[lvl - 1] = this.expression.evaluate();
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SkillsLevelFunction that = (SkillsLevelFunction) o;
    return Objects.equals(this.registryKey, that.registryKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.registryKey);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", this.registryKey)
        .add("formula", this.expression)
        .toString();
  }

  public void printTable() {

    final DecimalFormat format = new DecimalFormat("###,###,###.##");

    for (int i = 0; i < this.xpTable.length - 1; i++) {
      final Double start = this.xpTable[i];

      if (start == UNKNOWN_EXP) {
        break;
      }

      if (i == this.xpTable.length - 2) {
        this.logger.warn("Lvl " + (i + 1) + " -> (" + format.format(start) + " - ~)");
      } else {
        final Double end = this.xpTable[i + 1];

        if (end == UNKNOWN_EXP) {
          this.logger.warn("Lvl " + (i + 1) + " -> (" + format.format(start) + " - ~)");
        } else {
          this.logger.warn("Lvl " + (i + 1) + " -> (" + format.format(start) + " - " + format.format(end) + ", diff: " + format.format(end - start)
              + ")");
        }
      }
    }
  }

  public interface Factory {

    SkillsLevelFunction create(final RegistryKey registryKey, final String formula);
  }
}
