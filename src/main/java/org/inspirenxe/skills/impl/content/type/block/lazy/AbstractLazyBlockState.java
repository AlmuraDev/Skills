package org.inspirenxe.skills.impl.content.type.block.lazy;

import com.almuradev.droplet.registry.reference.RegistryReference;
import com.google.common.base.Suppliers;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;

import java.util.function.Supplier;

public abstract class AbstractLazyBlockState implements LazyBlockState {
  private final RegistryReference<BlockType> block;
  private final Supplier<BlockState> state = Suppliers.memoize(this::createState);

  AbstractLazyBlockState(final RegistryReference<BlockType> block) {
    this.block = block;
  }

  @Override
  public final BlockType block() {
    return this.block.require();
  }

  @Override
  public final BlockState get() {
    return this.state.get();
  }

  abstract <T extends Comparable<T>> BlockState createState();

  @Override
  public final boolean test(final BlockState state) {
    return this.block().equals(state.getType()) && this.testInternal(state);
  }

  abstract <V extends Comparable<V>> boolean testInternal(final BlockState state);
}
