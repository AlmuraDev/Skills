package org.inspirenxe.skills.impl.content.component.query;

import net.kyori.fragment.filter.FilterQuery;
import org.spongepowered.api.event.Event;

import java.util.Collection;
import java.util.Optional;

public interface EventFilterQueryProducer<E extends Event, F extends FilterQuery> {

    Class<F> getFilterQueryType();

    Class<E> getEventType();

    Optional<F> produce(E source);

}
