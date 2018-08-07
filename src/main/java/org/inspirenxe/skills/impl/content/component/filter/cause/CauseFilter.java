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
package org.inspirenxe.skills.impl.content.component.filter.cause;

import com.google.common.base.MoreObjects;
import net.kyori.fragment.filter.FilterQuery;
import net.kyori.fragment.filter.FilterResponse;
import net.kyori.fragment.filter.TypedFilter;
import org.inspirenxe.skills.impl.cause.CauseOperatorType;
import org.inspirenxe.skills.impl.cause.CauseType;

public final class CauseFilter implements TypedFilter<CauseQuery> {

  private final CauseOperatorType causeOperatorType;
  private final CauseType causeType;

  CauseFilter(final CauseOperatorType causeOperatorType, CauseType causeType) {
    this.causeOperatorType = causeOperatorType;
    this.causeType = causeType;
  }

  @Override
  public boolean queryable(FilterQuery query) {
    return query instanceof CauseQuery;
  }

  @Override
  public FilterResponse typedQuery(CauseQuery query) {
    return FilterResponse.from(this.causeOperatorType.matches(query.getCause(), this.causeType.getTypeClass()));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("op", this.causeOperatorType)
      .add("type", this.causeType)
      .toString();
  }
}