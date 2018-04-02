package org.inspirenxe.skills.impl.component.filter.block;

import com.almuradev.droplet.component.filter.FilterQuery;
import org.inspirenxe.skills.impl.content.type.block.lazy.LazyBlockState;
import org.spongepowered.api.block.BlockType;

public interface BlockQuery extends FilterQuery {
  default BlockType block() {
    return this.state().block();
  }

  LazyBlockState state();
}
