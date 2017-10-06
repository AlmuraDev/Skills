package org.inspirenxe.skills.impl.event.experience.change;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.inspirenxe.skills.impl.event.experience.ExperienceEventImpl;
import org.spongepowered.api.Sponge;

public abstract class ChangeExperienceEventImpl extends ExperienceEventImpl implements ExperienceEvent.Change {

    private final Skill skill;

    protected ChangeExperienceEventImpl(Skill skill, double originalExperience, double experience) {
        super(Sponge.getCauseStackManager().getCurrentCause(), skill.getSkillType(), originalExperience, experience);
        this.skill = skill;
    }

    @Override
    public Skill getTargetSkill() {
        return this.skill;
    }
}
