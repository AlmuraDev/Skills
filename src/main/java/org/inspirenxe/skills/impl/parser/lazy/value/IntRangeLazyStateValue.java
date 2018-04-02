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
package org.inspirenxe.skills.impl.parser.lazy.value;

import com.almuradev.droplet.component.range.IntRange;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.concurrent.ThreadLocalRandom;

final class IntRangeLazyStateValue implements RangeLazyStateValue<Integer> {

  private final IntRange range;

  IntRangeLazyStateValue(final IntRange range) {
    this.range = range;
  }

  @Override
  public Class<Integer> type() {
    return Integer.class;
  }

  @Override
  public Integer min() {
    return this.range.min();
  }

  @Override
  public Integer max() {
    return this.range.max();
  }

  @Override
  public boolean test(final BlockTrait<Integer> property, final BlockState state) {
    final Integer value = state.getTraitValue(property).orElse(null);
    return value != null && this.range.contains(value);
  }

  @Override
  public Integer get(final BlockTrait<Integer> property) {
    return this.range.random(ThreadLocalRandom.current());
  }
}
