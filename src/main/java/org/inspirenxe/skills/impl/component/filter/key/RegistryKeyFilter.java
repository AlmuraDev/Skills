package org.inspirenxe.skills.impl.component.filter.key;

import com.almuradev.droplet.component.filter.AbstractFilter;
import com.almuradev.droplet.registry.RegistryKey;

public final class RegistryKeyFilter implements AbstractFilter<RegistryKeyFilterQuery> {
  private final RegistryKey key;

  RegistryKeyFilter(final RegistryKey key) {
    this.key = key;
  }

  @Override
  public boolean testInternal(final RegistryKeyFilterQuery query) {
    return this.key.equals(query.key());
  }
}
