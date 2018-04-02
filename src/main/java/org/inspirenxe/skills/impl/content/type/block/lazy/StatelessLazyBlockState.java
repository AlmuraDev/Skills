package org.inspirenxe.skills.impl.content.type.block.lazy;

import com.almuradev.droplet.registry.reference.RegistryReference;
import org.inspirenxe.skills.impl.content.type.block.lazy.value.LazyStateValue;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Optional;

public final class StatelessLazyBlockState extends AbstractLazyBlockState {
  public StatelessLazyBlockState(final RegistryReference<BlockType> block) {
    super(block);
  }

  @Override
  BlockState createState() {
    return this.block().getDefaultState();
  }

  @Override
  public boolean testInternal(final BlockState state) {
    return true; // We have no properties to test
  }

  @Override
  public <V extends Comparable<V>> Optional<LazyStateValue<V>> value(final BlockTrait<V> property) {
    return Optional.empty();
  }
}
