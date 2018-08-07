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

import com.almuradev.droplet.registry.reference.RegistryReference;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Injector;
import net.kyori.fragment.filter.FilterQuery;
import net.kyori.fragment.filter.FilterResponse;
import net.kyori.fragment.filter.TypedFilter;
import net.kyori.violet.FriendlyTypeLiteral;
import net.kyori.violet.TypeArgument;
import org.inspirenxe.skills.impl.content.parser.value.StringToValueParser;
import org.spongepowered.api.data.key.Key;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

public final class DataFilter implements TypedFilter<DataQuery> {

  @Nullable
  @Inject
  private static Injector injector;
  private final RegistryReference<Key> dataKey;
  @Nullable private final String rawValue;
  @Nullable private Object value;
  private boolean computedValue;

  DataFilter(final RegistryReference<Key> dataKey, @Nullable final String rawValue) {
    this.dataKey = dataKey;
    this.rawValue = rawValue;
  }

  @Override
  public boolean queryable(final FilterQuery query) {
    return query instanceof DataQuery;
  }

  @SuppressWarnings("unchecked")
  @Override
  public FilterResponse typedQuery(final DataQuery query) {
    final Key key = this.dataKey.require();
    FilterResponse response = FilterResponse.from(query.getDataHolder().supports(key));

    if (response == FilterResponse.ALLOW && this.rawValue != null && injector != null) {
      if (!this.computedValue) {
        final StringToValueParser<?> parser = injector.getInstance(com.google.inject.Key.get(new FriendlyTypeLiteral<StringToValueParser<?>>() {
        }.where(new TypeArgument(key.getElementToken()) {})));
        this.value = parser.parse(key.getElementToken(), rawValue).orElse(null);
        this.computedValue = true;
      }
      Optional<?> holderValue = query.getDataHolder().get(key);
      response = holderValue.
              map(val -> FilterResponse.from(Objects.equals(this.value, val)))
              .orElse(FilterResponse.DENY);
    }

    return response;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("key", this.dataKey)
      .add("value", this.rawValue)
      .toString();
  }
}