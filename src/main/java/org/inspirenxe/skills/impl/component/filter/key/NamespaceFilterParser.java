package org.inspirenxe.skills.impl.component.filter.key;

import com.almuradev.droplet.component.filter.FilterTypeParser;
import net.kyori.xml.node.Node;

import javax.inject.Singleton;

@Singleton
public final class NamespaceFilterParser implements FilterTypeParser<NamespaceFilter> {
  @Override
  public NamespaceFilter throwingParse(final Node node) {
    return new NamespaceFilter(node.value());
  }
}
