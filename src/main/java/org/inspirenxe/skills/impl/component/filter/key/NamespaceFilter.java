package org.inspirenxe.skills.impl.component.filter.key;

import com.almuradev.droplet.component.filter.AbstractFilter;

public final class NamespaceFilter implements AbstractFilter<RegistryKeyFilterQuery> {
  private final String namespace;

  NamespaceFilter(final String namespace) {
    this.namespace = namespace;
  }

  @Override
  public boolean testInternal(final RegistryKeyFilterQuery query) {
    return this.namespace.equals(query.key().namespace());
  }
}
