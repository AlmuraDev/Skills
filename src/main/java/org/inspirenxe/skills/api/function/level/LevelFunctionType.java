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
package org.inspirenxe.skills.api.function.level;

import org.inspirenxe.skills.api.function.FunctionType;

import java.util.function.IntToDoubleFunction;

/**
 * {@link FunctionType} meant to process an integer level and return a double representing total experience at that level.
 */
public interface LevelFunctionType extends FunctionType, IntToDoubleFunction {

  double UNKNOWN_EXP = -1;

  int UNKNOWN_LEVEL = -1;

  /**
   * Gets the experience for the level provided or {@link LevelFunctionType#UNKNOWN_EXP} if no experience amount reaches the level.
   *
   * <Note>
   *   Implementors should make every effort to ensure that the experience returned is at the start of the level
   *   or document otherwise.
   * </Note>
   *
   * @param level The level
   * @return The experience
   */
  double getXPFor(final int level);

  /**
   * Gets the level for the experience provided or {@link LevelFunctionType#UNKNOWN_LEVEL} if unknown.
   *
   * @param xp The experience
   * @return The level
   */
  int getLevelFor(final double xp);

  /**
   * Gets the experience between a lower and upper level or {@link LevelFunctionType#UNKNOWN_EXP} if unknown.
   *
   * @param lower The lower level
   * @param upper The upper level
   * @return The experience
   */
  default double getXPBetween(final int lower, final int upper) {
    final double xpDiff = this.applyAsDouble(upper) - this.applyAsDouble(lower);
    return xpDiff < 0 ? UNKNOWN_EXP : xpDiff;
  }

  @Override
  default double applyAsDouble(final int value) {
    return this.getXPFor(value);
  }

  /**
   * Instructs the function to build an internal cache of the level table. It is up to the implementation to determine what
   * this method does, if anything. Even if the implementation should make use of a cache, the suggested max is not guaranteed
   * to be honored.
   *
   * @param suggestedMax A suggested maximum level to cache to
   */
  void buildLevelTable(final int suggestedMax);
}
