package org.inspirenxe.skills.impl.content.component.apply;

import com.google.inject.Inject;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillManager;
import org.inspirenxe.skills.api.SkillType;
import org.spongepowered.api.entity.living.player.Player;

public abstract class EventApplicatorImpl implements EventApplicator {

    @Inject protected SkillManager skillManager;

    protected Skill getSkill(SkillType skillType, Player player) {
        return this.skillManager.getHolder(player.getWorld().getUniqueId(), player.getUniqueId()).get().getSkill(skillType).get();
    }

}
