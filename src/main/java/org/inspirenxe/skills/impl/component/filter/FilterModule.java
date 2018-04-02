package org.inspirenxe.skills.impl.component.filter;

import com.almuradev.droplet.component.filter.FilterBinder;
import com.almuradev.droplet.parser.ParserBinder;
import net.kyori.violet.AbstractModule;
import org.inspirenxe.skills.impl.component.filter.key.NamespaceFilterParser;
import org.inspirenxe.skills.impl.component.filter.key.RegistryKeyFilterParser;

public final class FilterModule extends AbstractModule implements FilterBinder, ParserBinder {

  @Override
  protected void configure() {
    this.bindFilter("key").to(RegistryKeyFilterParser.class);
    this.bindFilter("namespace").to(NamespaceFilterParser.class);
  }
}
