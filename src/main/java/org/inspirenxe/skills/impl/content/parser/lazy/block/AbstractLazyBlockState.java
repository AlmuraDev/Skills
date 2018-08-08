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
import com.google.common.base.Suppliers;
import org.inspirenxe.skills.impl.content.component.filter.block.BlockQuery;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Transaction;

import java.util.function.Supplier;

import javax.annotation.Nullable;

public abstract class AbstractLazyBlockState implements LazyBlockState {

  protected final RegistryReference<BlockType> block;
  private final Supplier<BlockState> state = Suppliers.memoize(this::createState);
  private final BlockTransactionSource blockTransactionSource;

  AbstractLazyBlockState(final RegistryReference<BlockType> block, final BlockTransactionSource blockTransactionSource) {
    this.block = block;
    this.blockTransactionSource = blockTransactionSource;
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
  public final boolean test(final BlockQuery blockQuery) {
    final Transaction<BlockSnapshot> transaction = blockQuery.getBlockTransaction();

    final BlockState originalState = transaction.getOriginal().getState();
    final BlockState finalState = transaction.getFinal().getState();
    final BlockTransactionSource source = this.blockTransactionSource != BlockTransactionSource.INHERIT ? this.blockTransactionSource : blockQuery.getInheritedSource();

    switch (source) {
      case ORIGINAL:
        return this.checkMatch(originalState);
      case FINAL:
        return this.checkMatch(finalState);
      case EITHER:
        return this.checkMatch(originalState) || this.checkMatch(finalState);
    }
    throw new IllegalStateException("Impossible state reached for BlockQuery: " + blockQuery);
  }

  private boolean checkMatch(final BlockState state) {
    return this.block().equals(state.getType()) && this.testInternal(state);
  }

  abstract <V extends Comparable<V>> boolean testInternal(final BlockState state);
}
