package org.inspirenxe.skills.api;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface SkillHolder {

    UUID getContainerUniqueId();

    UUID getHolderUniqueId();

    Optional<Skill> getSkill(SkillType type);

    Map<SkillType, Skill> getSkills();

    Skill addSkill(SkillType skillType);
}
