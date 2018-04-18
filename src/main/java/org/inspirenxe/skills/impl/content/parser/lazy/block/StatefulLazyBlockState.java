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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.inspirenxe.skills.impl.content.parser.lazy.block.value.LazyStateValue;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

final class StatefulLazyBlockState extends AbstractLazyBlockState {

  private final Supplier<Map<BlockTrait<? extends Comparable<?>>, LazyStateValue<? extends Comparable<?>>>> properties;

  StatefulLazyBlockState(final RegistryReference<BlockType> block, final Map<String, LazyStateValue<? extends Comparable<?>>> properties) {
    super(block);
    this.properties = Suppliers.memoize(() -> this.resolveProperties(properties));
  }

  @SuppressWarnings("unchecked")
  private <T extends Comparable<T>> Map<BlockTrait<? extends Comparable<?>>, LazyStateValue<? extends Comparable<?>>> resolveProperties(
      final Map<String, LazyStateValue<? extends Comparable<?>>> source) {
    final Map<BlockTrait<? extends Comparable<?>>, LazyStateValue<? extends Comparable<?>>> target = new HashMap<>();
    final BlockState definition = this.block().getDefaultState();
    for (final Map.Entry<String, LazyStateValue<? extends Comparable<?>>> entry : source.entrySet()) {
      @Nullable final BlockTrait<T> property = (BlockTrait<T>) definition.getTraits().stream().filter(trait -> trait.getName().equalsIgnoreCase
          (entry.getKey())).findFirst().orElse(null);
      if (property != null) {
        target.put(property, entry.getValue());
      }
    }
    return target;
  }

  @SuppressWarnings("unchecked")
  @Override
  <T extends Comparable<T>> BlockState createState() {
    BlockState state = this.block().getDefaultState();
    for (final Map.Entry<BlockTrait<? extends Comparable<?>>, LazyStateValue<? extends Comparable<?>>> entry : this.properties.get().entrySet()) {
      if (state == null) {
        break;
      }

      @Nullable final BlockTrait<T> property = (BlockTrait<T>) entry.getKey();
      if (property != null) {
        @Nullable final T value = ((LazyStateValue<T>) entry.getValue()).get(property);
        if (value != null) {
          state = state.withTrait(property, value).orElse(null);
        }
      }
    }
    return state;
  }

  @SuppressWarnings("unchecked")
  @Override
  <V extends Comparable<V>> boolean testInternal(final BlockState state) {
    for (final Map.Entry<BlockTrait<? extends Comparable<?>>, LazyStateValue<? extends Comparable<?>>> entry : this.properties.get().entrySet()) {
      final BlockTrait<V> property = (BlockTrait<V>) entry.getKey();
      final LazyStateValue<V> value = (LazyStateValue<V>) entry.getValue();
      if (!value.test(property, state)) {
        return false;
      }
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Comparable<V>> Optional<LazyStateValue<V>> value(final BlockTrait<V> property) {
    return Optional.ofNullable((LazyStateValue<V>) this.properties.get().get(property));
  }
}
