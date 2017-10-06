package org.inspirenxe.skills.impl.skill;

import com.google.common.base.MoreObjects;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SkillHolderImpl implements SkillHolder {

    private final UUID containerUniqueId, holderUniqueId;
    private final Map<SkillType, Skill> skills = new HashMap<>();

    private SkillHolderImpl(UUID containerUniqueId, UUID holderUniqueId) {
        this.containerUniqueId = containerUniqueId;
        this.holderUniqueId = holderUniqueId;
    }

    public static SkillHolderImpl of(UUID containerUniqueId, UUID holderUniqueId) {
        return new SkillHolderImpl(containerUniqueId, holderUniqueId);
    }

    @Override
    public UUID getContainerUniqueId() {
        return this.containerUniqueId;
    }

    @Override
    public UUID getHolderUniqueId() {
        return this.holderUniqueId;
    }

    @Override
    public Optional<Skill> getSkill(SkillType type) {
        return Optional.ofNullable(this.skills.get(type));
    }

    @Override
    public Map<SkillType, Skill> getSkills() {
        return Collections.unmodifiableMap(this.skills);
    }

    @Override
    public Skill addSkill(SkillType type) {
        final Skill skill = SkillImpl.of(type, this);

        this.skills.put(type, skill);

        return skill;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SkillHolderImpl)) {
            return false;
        }
        final SkillHolderImpl that = (SkillHolderImpl) o;
        return Objects.equals(containerUniqueId, that.containerUniqueId) &&
                Objects.equals(holderUniqueId, that.holderUniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(containerUniqueId, holderUniqueId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("containerUniqueId", this.containerUniqueId)
                .add("holderUniqueId", this.holderUniqueId)
                .add("skills", this.skills)
                .toString();
    }
}
