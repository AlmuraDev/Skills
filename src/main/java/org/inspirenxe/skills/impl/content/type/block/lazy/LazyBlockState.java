package org.inspirenxe.skills.impl.content.type.block.lazy;

import org.inspirenxe.skills.impl.content.type.block.lazy.value.LazyStateValue;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface LazyBlockState extends Predicate<BlockState>, Supplier<BlockState> {
  static LazyBlockState from(final BlockType block) {
    return new WrappedStatelessLazyBlockState(block);
  }

  static LazyBlockState from(final BlockState state) {
    return new WrappedStatefulLazyBlockState(state);
  }

  BlockType block();

  @SuppressWarnings("RedundantCast")
  default Collection<BlockTrait<? extends Comparable<?>>> properties() {
    return (Collection<BlockTrait<? extends Comparable<?>>>) this.block().getDefaultState().getTraits();
  }

  <V extends Comparable<V>> Optional<LazyStateValue<V>> value(final BlockTrait<V> property);

  default boolean matches(final LazyBlockState that) {
    return this.test(that.get());
  }
}
