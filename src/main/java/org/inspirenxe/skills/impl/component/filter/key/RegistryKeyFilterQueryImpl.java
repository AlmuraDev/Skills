package org.inspirenxe.skills.impl.component.filter.key;

import com.almuradev.droplet.registry.RegistryKey;

public final class RegistryKeyFilterQueryImpl implements RegistryKeyFilterQuery {
  private final RegistryKey key;

  public RegistryKeyFilterQueryImpl(final RegistryKey key) {
    this.key = key;
  }

  @Override
  public RegistryKey key() {
    return this.key;
  }
}
