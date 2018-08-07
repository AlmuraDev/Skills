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
package org.inspirenxe.skills.impl.content.component.filter.block;

import com.google.common.base.Preconditions;
import net.kyori.fragment.filter.FilterQuery;
import org.inspirenxe.skills.impl.content.parser.lazy.block.BlockTransactionSource;
import org.inspirenxe.skills.impl.content.parser.lazy.block.LazyBlockState;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Transaction;

public final class BlockQuery implements FilterQuery {

  private final Transaction<BlockSnapshot> blockTransaction;
  private final BlockTransactionSource inheritedSource;

  public BlockQuery(final Transaction<BlockSnapshot> blockTransaction, BlockTransactionSource inheritedSource) {
    Preconditions.checkArgument(inheritedSource != BlockTransactionSource.INHERIT, "Cannot use BlockTransactionSource.INHERIT in a BlockQuery!");
    this.blockTransaction = blockTransaction;
    this.inheritedSource = inheritedSource;
  }

  public Transaction<BlockSnapshot> getBlockTransaction() {
    return this.blockTransaction;
  }

  /**
   * Gets the {@link BlockTransactionSource} inherited from the event.
   * @return
   */
  public BlockTransactionSource getInheritedSource() {
    return inheritedSource;
  }

  @Override
  public String toString() {
    return "BlockQuery{" +
            "blockTransaction=" + blockTransaction +
            ", inheritedSource=" + inheritedSource +
            '}';
  }
}
