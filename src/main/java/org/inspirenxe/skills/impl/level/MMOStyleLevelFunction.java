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
package org.inspirenxe.skills.impl.level;

import static com.google.common.base.Preconditions.checkState;

import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.level.LevelFunction;
import org.inspirenxe.skills.impl.Constants;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * A {@link LevelFunction} mirroring the style of the MMO Runescape's level curve. The curve is
 * built around making lower levels fast but the highest (near max) very slow and something to work
 * towards.
 *
 * As an example, should a {@link SkillType} have a level range of 1-99, level 92 would be the
 * "halfway" point in experience.
 */
public final class MMOStyleLevelFunction extends SkillsLevelFunction {

  private Double[] xpTable = new Double[100];

  public MMOStyleLevelFunction() {
    super(Constants.Plugin.ID + ":mmo_style", "MMO Style");
  }

  @Override
  public double getXPFor(int level) {
    checkState(level > -1, "Level must be positive!");

    if (this.xpTable.length < level) {
      return 0;
    }

    final Double cache = this.xpTable[level - 1];

    return cache == null ? 0 : cache;
  }

  @Override
  public int getLevelFor(double xp) {
    checkState(xp > -1, "XP must be positive!");

    int level = 1;

    for (int i = this.xpTable.length; i > 0; i--) {
      Double cache = this.xpTable[i - 1];

      if (cache == null) {
        continue;
      }

      if (xp >= cache) {
        level = i;

        break;
      }
    }

    return level;
  }

  @Override
  public void buildLevelTable(int suggestedMax) {
    if (suggestedMax > this.xpTable.length) {
      this.xpTable = Arrays.copyOf(this.xpTable, this.xpTable.length * 2);
    }

    if (this.xpTable[suggestedMax] == null) {
      double points = 0;
      for (int lvl = 1; lvl <= suggestedMax; lvl++) {
        this.xpTable[lvl - 1] = Math.floor(points / 4);
        points += Math.floor(lvl + 300 * Math.pow(2, lvl / 7.));
      }
    }
  }

  public void printTable() {

    final DecimalFormat format = new DecimalFormat("###,###.##");
    for (int i = 0; i < this.xpTable.length - 1; i++) {
      final Double start = this.xpTable[i];

      if (i == this.xpTable.length - 2) {
        System.err.println((i + 1) + " -> (" + format.format(start) + " - ~)");
      } else {
        final Double end = this.xpTable[i + 1];

        System.err.println((i + 1) + " -> (" + format.format(start) + " - " + format.format(end) + ", diff: " + format.format(end - start)
            + ")");
      }
    }
  }
}
