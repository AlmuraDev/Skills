package org.inspirenxe.skills.impl.event.experience.change;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.event.ExperienceEvent;

public final class ChangeExperiencePreEventImpl extends ChangeExperienceEventImpl implements ExperienceEvent.Change.Pre {

    private boolean isCancelled = false;

    public ChangeExperiencePreEventImpl(Skill skill, double originalExperience, double experience) {
        super(skill, originalExperience, experience);
    }

    @Override
    public void setExperience(double experience) {
        this.experience = experience;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
