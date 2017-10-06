package org.inspirenxe.skills.impl.event.experience.change;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.event.ExperienceEvent;

public final class ChangeExperiencePostEventImpl extends ChangeExperienceEventImpl implements ExperienceEvent.Change.Post {

    public ChangeExperiencePostEventImpl(Skill skill, double originalExperience, double experience) {
        super(skill, originalExperience, experience);
    }
}
