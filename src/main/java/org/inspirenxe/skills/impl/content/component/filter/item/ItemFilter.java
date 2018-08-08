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
package org.inspirenxe.skills.impl.content.component.filter.item;

import com.google.common.base.MoreObjects;
import net.kyori.fragment.filter.FilterResponse;
import org.inspirenxe.skills.impl.content.component.filter.TypedMultiFilter;
import org.inspirenxe.skills.impl.content.parser.lazy.item.LazyItemStack;

public final class ItemFilter extends TypedMultiFilter<ItemQuery> {

  private final LazyItemStack stack;

  ItemFilter(final LazyItemStack stack) {
    super(ItemQuery.class);
    this.stack = stack;
  }

  @Override
  public FilterResponse individualQuery(final ItemQuery query) {
    return FilterResponse.from(this.stack.matches(query.stack()));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .addValue(this.stack)
      .toString();
  }
}
