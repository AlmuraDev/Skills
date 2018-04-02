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

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface LazyBlockState extends Predicate<BlockState>, Supplier<BlockState> {

  static LazyBlockState from(final BlockType block) {
    return new WrappedStatelessLazyBlockState(block);
  }

  static LazyBlockState from(final BlockState state) {
    return new WrappedStatefulLazyBlockState(state);
  }

  BlockType block();

  @SuppressWarnings("RedundantCast")
  default Collection<BlockTrait<? extends Comparable<?>>> properties() {
    return (Collection<BlockTrait<? extends Comparable<?>>>) this.block().getDefaultState().getTraits();
  }

  <V extends Comparable<V>> Optional<LazyStateValue<V>> value(final BlockTrait<V> property);

  default boolean matches(final LazyBlockState that) {
    return this.test(that.get());
  }
}
