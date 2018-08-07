package org.inspirenxe.skills.impl.content.component.apply.math;

import com.google.inject.Singleton;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.math.BigDecimal;

@Singleton
public class BigDecimalParser implements Parser<BigDecimal> {

    @NonNull
    @Override
    public BigDecimal throwingParse(@NonNull Node node) throws XMLException {
        return new BigDecimal(node.value());
    }
}
