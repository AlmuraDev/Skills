package org.inspirenxe.skills.impl.content.type.block.lazy;

import org.inspirenxe.skills.impl.content.type.block.lazy.value.LazyStateValue;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Optional;

public final class WrappedStatelessLazyBlockState implements LazyBlockState {
  private final BlockType block;

  WrappedStatelessLazyBlockState(final BlockType block) {
    this.block = block;
  }

  @Override
  public BlockType block() {
    return this.block;
  }

  @Override
  public <V extends Comparable<V>> Optional<LazyStateValue<V>> value(final BlockTrait<V> property) {
    return Optional.empty();
  }

  @Override
  public boolean test(final BlockState state) {
    return this.block.equals(state.getType());
  }

  @Override
  public BlockState get() {
    return this.block().getDefaultState();
  }
}
