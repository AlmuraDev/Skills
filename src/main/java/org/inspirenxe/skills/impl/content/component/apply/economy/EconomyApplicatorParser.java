package org.inspirenxe.skills.impl.content.component.apply.economy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.inspirenxe.skills.impl.content.component.apply.math.MathOperationParser;

@Singleton
public class EconomyApplicatorParser implements Parser<EconomyApplicator> {

    @Inject
    private MathOperationParser mathOperationParser;

    @NonNull
    @Override
    public EconomyApplicator throwingParse(@NonNull Node node) throws XMLException {
        return new EconomyApplicator(this.mathOperationParser.parse(node));
    }
}
