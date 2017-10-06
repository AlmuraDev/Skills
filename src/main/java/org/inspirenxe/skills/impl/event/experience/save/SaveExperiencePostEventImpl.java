package org.inspirenxe.skills.impl.event.experience.save;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.spongepowered.api.Sponge;

public final class SaveExperiencePostEventImpl extends SaveExperienceEventImpl implements ExperienceEvent.Save.Post {

    private final Skill skill;

    public SaveExperiencePostEventImpl(Skill skill, double originalExperience, double experience) {
        super(Sponge.getCauseStackManager().getCurrentCause(), skill.getSkillType(), originalExperience, experience);

        this.skill = skill;
    }

    @Override
    public Skill getTargetSkill() {
        return this.skill;
    }
}
