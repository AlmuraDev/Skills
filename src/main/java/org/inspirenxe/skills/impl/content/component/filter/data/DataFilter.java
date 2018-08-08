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
package org.inspirenxe.skills.impl.content.component.filter.data;

import com.google.common.base.MoreObjects;
import net.kyori.fragment.filter.FilterResponse;
import org.inspirenxe.skills.impl.content.component.apply.data.KeyValue;
import org.inspirenxe.skills.impl.content.component.filter.EventCompoundFilterQuery;
import org.inspirenxe.skills.impl.content.component.filter.TypedMultiFilter;
import org.spongepowered.api.data.key.Key;

import java.util.Objects;
import java.util.Optional;

public final class DataFilter extends TypedMultiFilter<DataQuery> {

  private final KeyValue keyValue;

  DataFilter(final KeyValue keyValue) {
    super(DataQuery.class);
    this.keyValue = keyValue;
  }

  @SuppressWarnings("unchecked")
  @Override
  public FilterResponse individualQuery(EventCompoundFilterQuery parent, final DataQuery query) {
    final Key key = this.keyValue.getKey();
      Optional<?> holderValue = query.getDataHolder().get(key);
      FilterResponse response = holderValue.
              map(val -> FilterResponse.from(Objects.equals(this.keyValue.getValue(), val)))
              .orElse(FilterResponse.DENY);

    return response;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("keyValue", this.keyValue)
      .toString();
  }
}
