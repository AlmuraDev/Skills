package org.inspirenxe.skills.impl.content.component.filter.experience;

import org.inspirenxe.skills.impl.SkillManagerImpl;
import org.inspirenxe.skills.impl.content.component.query.EventFilterQueryProducer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;

import java.util.Optional;

public class LevelQueryProducer implements EventFilterQueryProducer<Event, LevelQuery> {

    @Override
    public Class<LevelQuery> getFilterQueryType() {
        return LevelQuery.class;
    }

    @Override
    public Class<Event> getEventType() {
        return Event.class;
    }

    @Override
    public Optional<LevelQuery> produce(Event source) {
        return source.getCause().first(Player.class)
                .map(p -> new LevelQueryImpl(SkillManagerImpl.INSTANCE.getHolder(p.getWorld().getUniqueId(), p.getUniqueId()).get()));
    }
}
