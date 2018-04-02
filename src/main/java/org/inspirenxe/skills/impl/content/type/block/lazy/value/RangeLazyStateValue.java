package org.inspirenxe.skills.impl.content.type.block.lazy.value;

public interface RangeLazyStateValue<V extends Comparable<V>> extends LazyStateValue<V> {
  Class<V> type();

  V min();

  V max();
}
