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
package org.inspirenxe.skills.api.function.economy;

import org.inspirenxe.skills.api.function.FunctionType;

import java.math.BigDecimal;
import java.util.function.BiFunction;

/**
 * {@link FunctionType} meant to process an integer level and double modifier and return a big decimal representing total experience at that level.
 */
public interface EconomyFunctionType extends FunctionType, BiFunction<Integer, Double, BigDecimal> {

  /**
   * Gets the money value calculated by the passed in level and modifier.
   *
   * @param level The level
   * @param modifier The modifier
   * @return The money value
   */
  BigDecimal getMoneyFor(final int level, final double modifier);

  @Override
  default BigDecimal apply(final Integer level, final Double modifier) {
    return this.getMoneyFor(level, modifier);
  }
}
