package org.inspirenxe.skills.api;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SkillManager {

    Set<SkillHolder> getHoldersInContainer(UUID containerUniqueId);

    Optional<SkillHolder> getHolder(UUID containerUniqueId, UUID holderUniqueId);
}
