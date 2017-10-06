package org.inspirenxe.skills.impl.event.experience;

import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.spongepowered.api.event.cause.Cause;

public abstract class ExperienceEventImpl implements ExperienceEvent {

    private final Cause cause;
    private final SkillType skillType;
    private final double originalExperience;
    protected double experience;

    protected ExperienceEventImpl(Cause cause, SkillType skillType, double originalExperience, double
            experience) {
        this.cause = cause;
        this.skillType = skillType;
        this.originalExperience = originalExperience;
        this.experience = experience;
    }

    @Override
    public final double getOriginalExperience() {
        return this.originalExperience;
    }

    @Override
    public final double getExperience() {
        return this.experience;
    }

    @Override
    public SkillType getTargetSkillType() {
        return this.skillType;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }
}
