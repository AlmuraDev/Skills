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

import com.almuradev.droplet.registry.reference.RegistryReference;
import com.google.common.base.Suppliers;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;

import java.util.function.Supplier;

import javax.annotation.Nullable;

public abstract class AbstractLazyBlockState implements LazyBlockState {

  private final RegistryReference<BlockType> block;
  private final Supplier<BlockState> state = Suppliers.memoize(this::createState);

  AbstractLazyBlockState(final RegistryReference<BlockType> block) {
    this.block = block;
  }

  @Override
  public final BlockType block() {
    return this.block.require();
  }

  @Override
  public final BlockState get() {
    return this.state.get();
  }

  @Nullable
  abstract <T extends Comparable<T>> BlockState createState();

  @Override
  public final boolean test(final BlockState state) {
    return this.block().equals(state.getType()) && this.testInternal(state);
  }

  abstract <V extends Comparable<V>> boolean testInternal(final BlockState state);
}
