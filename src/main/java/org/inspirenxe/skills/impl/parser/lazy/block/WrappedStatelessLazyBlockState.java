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
package org.inspirenxe.skills.impl.parser.lazy.block;

import org.inspirenxe.skills.impl.parser.lazy.block.value.LazyStateValue;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Optional;

public final class WrappedStatelessLazyBlockState implements LazyBlockState {

  private final BlockType block;

  WrappedStatelessLazyBlockState(final BlockType block) {
    this.block = block;
  }

  @Override
  public BlockType block() {
    return this.block;
  }

  @Override
  public <V extends Comparable<V>> Optional<LazyStateValue<V>> value(final BlockTrait<V> property) {
    return Optional.empty();
  }

  @Override
  public boolean test(final BlockState state) {
    return this.block.equals(state.getType());
  }

  @Override
  public BlockState get() {
    return this.block().getDefaultState();
  }
}
