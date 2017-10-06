package org.inspirenxe.skills.api.event;

import org.inspirenxe.skills.api.Skill;
import org.spongepowered.api.event.Event;

public interface TargetSkillEvent extends Event {

    Skill getTargetSkill();
}
