package org.inspirenxe.skills.impl.content.type.block.lazy.value;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Objects;

final class SimpleLazyStateValue<V extends Comparable<V>> implements LazyStateValue<V> {
  private final String string;

  SimpleLazyStateValue(final String string) {
    this.string = string;
  }

  @Override
  public boolean test(final BlockTrait<V> property, final BlockState state) {
    return Objects.equals(this.get(property), state.getTraitValue(property).orElse(null));
  }

  @Override
  public V get(final BlockTrait<V> property) {
    // TODO Figure out how to do this
    return property.parseValue(this.string).orNull();
  }
}
