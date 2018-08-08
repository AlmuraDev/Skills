package org.inspirenxe.skills.impl.content.type.skill;

import com.google.common.collect.ImmutableList;
import org.inspirenxe.skills.api.SkillType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.Collection;

public class DelegatingSkillEventListener implements EventListener<Event> {

    private Collection<SkillType> skillTypes;

    public DelegatingSkillEventListener(Collection<SkillType> skillTypes) {
        this.skillTypes = ImmutableList.copyOf(skillTypes);
    }

    @Override
    public void handle(Event event) throws Exception {
        if (!Sponge.isServerAvailable() || !Sponge.getServer().isMainThread()) {
            return;
        }
        if (event instanceof ChangeBlockEvent.Break) {
            System.err.println("Break!");
        }
        for (SkillType type: this.skillTypes) {
            type.processEvent(event);
        }
    }
}
