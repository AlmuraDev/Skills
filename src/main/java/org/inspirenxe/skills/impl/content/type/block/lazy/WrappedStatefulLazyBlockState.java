package org.inspirenxe.skills.impl.content.type.block.lazy;

import org.inspirenxe.skills.impl.content.type.block.lazy.value.LazyStateValue;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Objects;
import java.util.Optional;

public final class WrappedStatefulLazyBlockState implements LazyBlockState {
  private final BlockState state;

  WrappedStatefulLazyBlockState(final BlockState state) {
    this.state = state;
  }

  @Override
  public BlockType block() {
    return this.state.getType();
  }

  @Override
  public <V extends Comparable<V>> Optional<LazyStateValue<V>> value(final BlockTrait<V> property) {
    return Optional.of(new RealProperty<>(property));
  }

  @Override
  public boolean test(final BlockState state) {
    return this.state.equals(state);
  }

  @Override
  public BlockState get() {
    return this.state;
  }

  private class RealProperty<V extends Comparable<V>> implements LazyStateValue<V> {
    private final V value;

    private RealProperty(final BlockTrait<V> property) {
      this.value = WrappedStatefulLazyBlockState.this.state.getTraitValue(property).orElse(null);
    }

    @Override
    public boolean test(final BlockTrait<V> property, final BlockState state) {
      return Objects.equals(this.value, state.getTraitValue(property).orElse(null));
    }

    @Override
    public V get(final BlockTrait<V> property) {
      return this.value;
    }
  }
}
