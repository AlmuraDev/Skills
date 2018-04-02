package org.inspirenxe.skills.impl.content.type.block.lazy.value;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;

public interface LazyStateValue<V extends Comparable<V>> {
  boolean test(final BlockTrait<V> property, final BlockState state);

  @Nullable
  V get(final BlockTrait<V> property);
}
