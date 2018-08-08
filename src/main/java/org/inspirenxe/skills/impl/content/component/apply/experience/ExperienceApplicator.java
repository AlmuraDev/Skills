package org.inspirenxe.skills.impl.content.component.apply.experience;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.impl.content.component.apply.cause.CauseFirstEventApplicator;
import org.inspirenxe.skills.impl.content.component.apply.math.MathOperation;
import org.inspirenxe.skills.impl.content.component.filter.EventCompoundFilterQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;

import java.math.BigDecimal;

public class ExperienceApplicator extends CauseFirstEventApplicator<Event, Player> {

    private final MathOperation operation;

    protected ExperienceApplicator(MathOperation operation) {
        super(Player.class, Event.class);
        this.operation = operation;
    }

    @Override
    protected void applyWithCause(EventCompoundFilterQuery eventData, Event event, Player causeObject) {
        Skill skill = this.getSkill(eventData.getSkillType(), causeObject);
        double newExperience = this.operation.apply(new BigDecimal(skill.getCurrentExperience())).doubleValue();
        skill.setExperience(newExperience);
    }
}
