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
package org.inspirenxe.skills.impl.content.parser.lazy.block;

import com.almuradev.droplet.registry.reference.RegistryReference;
import com.google.common.base.MoreObjects;
import org.inspirenxe.skills.impl.content.parser.lazy.block.value.LazyStateValue;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Optional;

public final class StatelessLazyBlockState extends AbstractLazyBlockState {

  StatelessLazyBlockState(final RegistryReference<BlockType> block, final BlockTransactionSource source) {
    super(block, source);
  }

  @Override
  BlockState createState() {
    return this.block().getDefaultState();
  }

  @Override
  public boolean testInternal(final BlockState state) {
    return true; // We have no properties to test
  }

  @Override
  public <V extends Comparable<V>> Optional<LazyStateValue<V>> value(final BlockTrait<V> property) {
    return Optional.empty();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .addValue(this.block)
      .toString();
  }
}
