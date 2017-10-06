package org.inspirenxe.skills.impl.event.experience.load;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.spongepowered.api.Sponge;

public final class LoadExperiencePostEventImpl extends LoadExperienceEventImpl implements ExperienceEvent.Load.Post {

    private final Skill skill;

    public LoadExperiencePostEventImpl(Skill skill, double originalExperience, double experience, boolean hasGainedExperienceBefore) {
        super(Sponge.getCauseStackManager().getCurrentCause(), skill.getSkillType(), originalExperience, experience, hasGainedExperienceBefore);
        this.skill = skill;
    }

    @Override
    public Skill getTargetSkill() {
        return this.skill;
    }
}
