package org.inspirenxe.skills.impl.component;

import net.kyori.violet.AbstractModule;
import org.inspirenxe.skills.impl.component.filter.FilterModule;

public final class ComponentModule extends AbstractModule {

  @Override
  protected void configure() {
    this.install(new FilterModule());
  }
}
