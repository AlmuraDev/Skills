package org.inspirenxe.skills.impl.component.filter.key;

import com.almuradev.droplet.component.filter.FilterTypeParser;
import com.almuradev.droplet.parser.Parser;
import com.almuradev.droplet.registry.RegistryKey;
import net.kyori.xml.node.Node;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class RegistryKeyFilterParser implements FilterTypeParser<RegistryKeyFilter> {
  private final Parser<RegistryKey> parser;

  @Inject
  private RegistryKeyFilterParser(final Parser<RegistryKey> parser) {
    this.parser = parser;
  }

  @Override
  public RegistryKeyFilter throwingParse(final Node node) {
    return new RegistryKeyFilter(this.parser.parse(node));
  }
}
