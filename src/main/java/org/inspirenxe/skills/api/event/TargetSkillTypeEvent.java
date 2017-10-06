package org.inspirenxe.skills.api.event;

import org.inspirenxe.skills.api.SkillType;
import org.spongepowered.api.event.Event;

public interface TargetSkillTypeEvent extends Event {

    SkillType getTargetSkillType();
}
