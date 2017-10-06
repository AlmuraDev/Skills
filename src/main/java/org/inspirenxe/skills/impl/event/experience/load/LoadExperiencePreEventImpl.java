package org.inspirenxe.skills.impl.event.experience.load;

import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.spongepowered.api.event.cause.Cause;

import java.util.UUID;

public final class LoadExperiencePreEventImpl extends LoadExperienceEventImpl implements ExperienceEvent.Load.Pre {

    private final UUID containerUniqueId, holderUniqueId;

    public LoadExperiencePreEventImpl(Cause cause, UUID containerUniqueId, UUID holderUniqueId, SkillType skillType, double originalExperience,
            double experience, boolean hasGainedExperienceBefore) {
        super(cause, skillType, originalExperience, experience, hasGainedExperienceBefore);

        this.containerUniqueId = containerUniqueId;
        this.holderUniqueId = holderUniqueId;
    }

    @Override
    public void setExperience(double experience) {
        this.experience = experience;
    }

    @Override
    public UUID getContainerUniqueId() {
        return this.containerUniqueId;
    }

    @Override
    public UUID getHolderUniqueId() {
        return this.holderUniqueId;
    }
}
