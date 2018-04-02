package org.inspirenxe.skills.impl.content.type.block.lazy.value;

import com.almuradev.droplet.component.range.IntRange;
import com.almuradev.droplet.parser.Parser;
import com.google.common.collect.MoreCollectors;
import net.kyori.xml.node.Node;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class LazyStateValueParser implements Parser<LazyStateValue<?>> {
  private final Parser<IntRange> intRangeParser;

  @Inject
  private LazyStateValueParser(final Parser<IntRange> intRangeParser) {
    this.intRangeParser = intRangeParser;
  }

  @Override
  public LazyStateValue<?> throwingParse(final Node node) {
    LazyStateValue<?> value = this.parseSimple(node);
    if(value != null) {
      return value;
    }
    value = this.parseRange(node);
    if(value != null) {
      return value;
    }
    return null;
  }

  private LazyStateValue<?> parseSimple(final Node node) {
    return node.nodes("value").collect(MoreCollectors.toOptional())
      .map(value -> new SimpleLazyStateValue<>(value.value())).orElse(null);
  }

  private LazyStateValue<?> parseRange(final Node node) {
    return node.nodes("range").collect(MoreCollectors.toOptional())
      .map(value -> new IntRangeLazyStateValue(this.intRangeParser.parse(value)))
      .orElse(null);
  }
}
