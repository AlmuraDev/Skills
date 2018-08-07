package org.inspirenxe.skills.impl.content.component.apply.cancel;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.content.component.apply.TypedEventApplicator;
import org.inspirenxe.skills.impl.content.type.skill.component.event.EventData;
import org.spongepowered.api.event.Cancellable;

public class EventCancelApplicator extends TypedEventApplicator<Cancellable> {

    private final boolean cancelled;

    public EventCancelApplicator(boolean cancelled) {
        super(Cancellable.class);
        this.cancelled = cancelled;
    }

    @Override
    public void applyTyped(EventData eventData, Cancellable event) {
        event.setCancelled(this.cancelled);
    }
}
