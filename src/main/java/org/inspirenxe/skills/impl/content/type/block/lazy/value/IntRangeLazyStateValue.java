package org.inspirenxe.skills.impl.content.type.block.lazy.value;

import com.almuradev.droplet.component.range.IntRange;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.concurrent.ThreadLocalRandom;

final class IntRangeLazyStateValue implements RangeLazyStateValue<Integer> {
  private final IntRange range;

  IntRangeLazyStateValue(final IntRange range) {
    this.range = range;
  }

  @Override
  public Class<Integer> type() {
    return Integer.class;
  }

  @Override
  public Integer min() {
    return this.range.min();
  }

  @Override
  public Integer max() {
    return this.range.max();
  }

  @Override
  public boolean test(final BlockTrait<Integer> property, final BlockState state) {
    final Integer value = state.getTraitValue(property).orElse(null);
    return value != null && this.range.contains(value);
  }

  @Override
  public Integer get(final BlockTrait<Integer> property) {
    return this.range.random(ThreadLocalRandom.current());
  }
}
