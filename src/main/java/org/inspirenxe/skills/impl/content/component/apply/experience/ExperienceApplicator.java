package org.inspirenxe.skills.impl.content.component.apply.experience;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.content.component.apply.MathOperation;
import org.inspirenxe.skills.impl.content.component.apply.TypedEventApplicator;
import org.inspirenxe.skills.impl.content.component.apply.cause.CauseFirstEventApplicator;
import org.inspirenxe.skills.impl.content.type.skill.component.event.EventData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;

public class ExperienceApplicator extends CauseFirstEventApplicator<Event, Player> {

    private final MathOperation operation;
    private final double value;

    protected ExperienceApplicator(MathOperation operation, double value) {
        super(Player.class, Event.class);
        this.operation = operation;
        this.value = value;
    }

    @Override
    protected void applyWithCause(EventData eventData, Event event, Player causeObject) {
        Skill skill = this.getSkill(eventData.getSkillType(), causeObject);
        double newExperience = this.operation.apply(skill.getCurrentExperience(), this.value);
        skill.setExperience(newExperience);
    }
}
