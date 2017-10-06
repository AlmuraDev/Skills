package org.inspirenxe.skills.impl.event.experience.save;

import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.inspirenxe.skills.impl.event.experience.ExperienceEventImpl;
import org.spongepowered.api.event.cause.Cause;

abstract class SaveExperienceEventImpl extends ExperienceEventImpl implements ExperienceEvent.Save {

    SaveExperienceEventImpl(Cause cause, SkillType skillType, double originalExperience, double experience) {
        super(cause, skillType, originalExperience, experience);
    }
}
