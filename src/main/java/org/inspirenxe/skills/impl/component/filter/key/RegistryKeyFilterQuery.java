package org.inspirenxe.skills.impl.component.filter.key;

import com.almuradev.droplet.component.filter.FilterQuery;
import com.almuradev.droplet.registry.RegistryKey;

public interface RegistryKeyFilterQuery extends FilterQuery {
  RegistryKey key();
}
