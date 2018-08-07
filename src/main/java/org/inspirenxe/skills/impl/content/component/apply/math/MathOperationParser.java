package org.inspirenxe.skills.impl.content.component.apply.math;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.inspirenxe.skills.impl.EnumUtils;
import org.inspirenxe.skills.impl.content.component.apply.MathOperationType;

import java.math.BigDecimal;

@Singleton
public class MathOperationParser implements Parser<MathOperation> {

    @Inject
    private Parser<BigDecimal> bigDecimalParser;

    @NonNull
    @Override
    public MathOperation throwingParse(@NonNull Node node) throws XMLException {
        String op =  node.requireAttribute("op").value();
        MathOperationType operation = EnumUtils.parse(MathOperationType.class, op)
                .orElseThrow(() -> new IllegalStateException("Unknown math operaation " + op));
        BigDecimal value = this.bigDecimalParser.throwingParse(node.requireAttribute("value"));

        return new MathOperation(operation, value);
    }
}
