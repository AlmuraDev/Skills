package org.inspirenxe.skills.impl.event.experience.load;

import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.inspirenxe.skills.impl.event.experience.ExperienceEventImpl;
import org.spongepowered.api.event.cause.Cause;

abstract class LoadExperienceEventImpl extends ExperienceEventImpl implements ExperienceEvent.Load {

    private final boolean hasGainedExperienceBefore;

    LoadExperienceEventImpl(Cause cause, SkillType skillType, double originalExperience, double experience, boolean hasGainedExperienceBefore) {
        super(cause, skillType, originalExperience, experience);
        this.hasGainedExperienceBefore = hasGainedExperienceBefore;
    }

    @Override
    public boolean hasGainedExperienceBefore() {
        return this.hasGainedExperienceBefore;
    }
}
