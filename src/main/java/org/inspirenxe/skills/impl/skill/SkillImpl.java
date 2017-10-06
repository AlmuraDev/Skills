package org.inspirenxe.skills.impl.skill;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.inspirenxe.skills.impl.event.experience.change.ChangeExperiencePostEventImpl;
import org.inspirenxe.skills.impl.event.experience.change.ChangeExperiencePreEventImpl;
import org.spongepowered.api.Sponge;

import java.util.Objects;

public final class SkillImpl implements Skill {

    private final SkillType skillType;
    private final SkillHolder skillHolder;
    private double experience;
    private boolean dirtyState, isInitialized;

    private SkillImpl(SkillType skillType, SkillHolder skillHolder) {
        checkNotNull(skillType);
        checkNotNull(skillHolder);

        this.skillType = skillType;
        this.skillHolder = skillHolder;
    }

    public static SkillImpl of(SkillType skillType, SkillHolder skillHolder) {
        return new SkillImpl(skillType, skillHolder);
    }

    @Override
    public final SkillType getSkillType() {
        return this.skillType;
    }

    @Override
    public final SkillHolder getHolder() {
        return this.skillHolder;
    }

    @Override
    public final double getCurrentExperience() {
        return this.experience;
    }

    @Override
    public Skill setExperience(double experience) {
        checkState(experience >= 0, "Setting experience must be greater than 0!");

        if (!this.isInitialized) {
            this.isInitialized = true;
        }

        final double originalExperience = this.experience;
        final ExperienceEvent.Change.Pre event = new ChangeExperiencePreEventImpl(this, originalExperience, experience);
        if (Sponge.getEventManager().post(event)) {
            return this;
        }

        this.experience = event.getExperience();

        this.dirtyState = true;

        Sponge.getEventManager().post(new ChangeExperiencePostEventImpl(this, originalExperience, this.experience));
        return this;
    }

    @Override
    public boolean isInitialized() {
        return this.isInitialized;
    }

    @Override
    public boolean isDirtyState() {
        return this.dirtyState;
    }

    @Override
    public void setDirtyState(boolean dirtyState) {
        this.dirtyState = dirtyState;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SkillImpl)) {
            return false;
        }
        final SkillImpl skill = (SkillImpl) o;
        return Objects.equals(skillType, skill.skillType) &&
                Objects.equals(skillHolder, skill.skillHolder);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(skillType, skillHolder);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this)
                .add("skillType", this.skillType)
                .add("containerUniqueId", this.skillHolder.getContainerUniqueId())
                .add("holderUniqueId", this.skillHolder.getHolderUniqueId())
                .toString();
    }
}
