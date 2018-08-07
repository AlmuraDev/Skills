package org.inspirenxe.skills.impl.content.component.apply.cause;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.content.component.apply.TypedEventApplicator;
import org.inspirenxe.skills.impl.content.type.skill.component.event.EventData;
import org.spongepowered.api.event.Event;

public abstract class CauseFirstEventApplicator<T extends Event, C> extends TypedEventApplicator<T> {

    private final Class<C> causeClass;

    protected CauseFirstEventApplicator(Class<C> causeClass, Class<T> eventClass) {
        super(eventClass);
        this.causeClass = causeClass;
    }

    @Override
    protected final void applyTyped(EventData eventData, T event) {
        C causeObject = event.getCause().first(this.causeClass).orElseThrow(() -> new IllegalStateException(String.format("Expected event '%s' to have '%s' in its cause!", event, this.causeClass)));
        this.applyWithCause(eventData, event, causeObject);
    }

    protected abstract void applyWithCause(EventData eventData, T event, C causeObject);
}
