package org.inspirenxe.skills.impl.content.component.apply.experience;

import com.google.inject.Inject;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.inspirenxe.skills.impl.EnumUtils;
import org.inspirenxe.skills.impl.content.component.apply.MathOperation;

public class ExperienceApplicatorParser implements Parser<ExperienceApplicator> {


    @Inject private Parser<Double> doubleParser;

    @NonNull
    @Override
    public ExperienceApplicator throwingParse(@NonNull Node node) throws XMLException {
        String op =  node.requireAttribute("op").value();
        MathOperation operation = EnumUtils.parse(MathOperation.class, op)
                .orElseThrow(() -> new IllegalStateException("Unknown math operaation " + op));
        double value = this.doubleParser.throwingParse(node.requireAttribute("value"));

        return new ExperienceApplicator(operation, value);
    }
}
