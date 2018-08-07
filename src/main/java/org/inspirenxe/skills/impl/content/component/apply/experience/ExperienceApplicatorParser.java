package org.inspirenxe.skills.impl.content.component.apply.experience;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.kyori.xml.XMLException;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.inspirenxe.skills.impl.EnumUtils;
import org.inspirenxe.skills.impl.content.component.apply.MathOperationType;
import org.inspirenxe.skills.impl.content.component.apply.math.MathOperationParser;

@Singleton
public class ExperienceApplicatorParser implements Parser<ExperienceApplicator> {

    @Inject private MathOperationParser mathOperationParser;

    @NonNull
    @Override
    public ExperienceApplicator throwingParse(@NonNull Node node) throws XMLException {
        return new ExperienceApplicator(this.mathOperationParser.parse(node));
    }
}
