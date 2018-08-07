package org.inspirenxe.skills.impl.content.component.apply;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.content.type.skill.component.event.EventData;
import org.spongepowered.api.event.Event;

public abstract class TypedEventApplicator<T> extends EventApplicatorImpl {

    private final Class<T> eventClass;

    protected TypedEventApplicator(Class<T> eventClass) {
        this.eventClass = eventClass;
    }

    @Override
    public void apply(EventData eventData) {
        if (!this.eventClass.isInstance(eventData.getEvent())) {
            throw new IllegalStateException(String.format("Expected event of type '%s'. but got '%s'", this.eventClass, eventData));
        }
        this.applyTyped(eventData, this.eventClass.cast(eventData.getEvent()));
    }

    protected abstract void applyTyped(final EventData eventData, T event);

}
