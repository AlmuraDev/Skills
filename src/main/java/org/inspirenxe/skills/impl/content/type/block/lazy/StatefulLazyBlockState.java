package org.inspirenxe.skills.impl.content.type.block.lazy;

import com.almuradev.droplet.registry.reference.RegistryReference;
import com.google.common.base.Suppliers;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.inspirenxe.skills.impl.content.type.block.lazy.value.LazyStateValue;
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

  private <T extends Comparable<T>> Map<BlockTrait<? extends Comparable<?>>, LazyStateValue<? extends Comparable<?>>> resolveProperties(final Map<String, LazyStateValue<? extends Comparable<?>>> source) {
    // TODO
    final Map<BlockTrait<? extends Comparable<?>>, LazyStateValue<? extends Comparable<?>>> target = new HashMap<>();
    final BlockStateContainer definition = this.block().getBlockState();
    for(final Map.Entry<String, LazyStateValue<? extends Comparable<?>>> entry : source.entrySet()) {
      @Nullable final BlockTrait<T> property = (BlockTrait<T>) definition.getProperty(entry.getKey());
      if(property != null) {
        target.put(property, entry.getValue());
      }
    }
    return target;
  }

  @Override
  <T extends Comparable<T>> BlockState createState() {
    // TODO
    BlockState state = this.block().getDefaultState();
    for(final Map.Entry<BlockTrait<? extends Comparable<?>>, LazyStateValue<? extends Comparable<?>>> entry : this.properties.get().entrySet()) {
      @Nullable final BlockTrait<T> property = (BlockTrait<T>) entry.getKey();
      if(property != null) {
        @Nullable final T value = ((LazyStateValue<T>) entry.getValue()).get(property);
        if(value != null) {
          state = state.withProperty(property, value);
        }
      }
    }
    return state;
  }

  @Override
  <V extends Comparable<V>> boolean testInternal(final BlockState state) {
    for(final Map.Entry<BlockTrait<? extends Comparable<?>>, LazyStateValue<? extends Comparable<?>>> entry : this.properties.get().entrySet()) {
      final BlockTrait<V> property = (BlockTrait<V>) entry.getKey();
      final LazyStateValue<V> value = (LazyStateValue<V>) entry.getValue();
      if(!value.test(property, state)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public <V extends Comparable<V>> Optional<LazyStateValue<V>> value(final BlockTrait<V> property) {
    return Optional.ofNullable((LazyStateValue<V>) this.properties.get().get(property));
  }
}
