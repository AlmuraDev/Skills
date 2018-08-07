package org.inspirenxe.skills.impl.content.type.skill;

import com.google.common.collect.ImmutableList;
import org.inspirenxe.skills.api.SkillType;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.Collection;

public class DelegatingSkillEventListener implements EventListener<Event> {

    private Class<? extends Event> eventType;
    private Collection<SkillType> skillTypes;

    public DelegatingSkillEventListener(Class<? extends Event> eventType, Collection<SkillType> skillTypes) {
        this.eventType = eventType;
        this.skillTypes = ImmutableList.copyOf(skillTypes);
    }

    @Override
    public void handle(Event event) throws Exception {
        // TODO - register listeners for specific events to
        // prevent Sponge from unecessarily firing lots of events (via ShouldFire)
        if (event instanceof ChangeBlockEvent.Break) {
            System.err.println("Break!");
        }
        for (SkillType type: this.skillTypes) {
            type.processEvent(event);
        }
    }
}
