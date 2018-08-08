package org.inspirenxe.skills.impl.content.component.filter.item;

import org.inspirenxe.skills.impl.content.component.query.EventFilterQueryProducer;
import org.inspirenxe.skills.impl.content.parser.lazy.item.LazyItemStackImpl;
import org.inspirenxe.skills.impl.content.parser.lazy.item.WrappedLazyItemStack;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;

import java.util.Optional;

public class ItemQueryProducer implements EventFilterQueryProducer<InteractItemEvent, ItemQuery> {

    @Override
    public Class<ItemQuery> getFilterQueryType() {
        return ItemQuery.class;
    }

    @Override
    public Class<InteractItemEvent> getEventType() {
        return InteractItemEvent.class;
    }

    @Override
    public Optional<ItemQuery> produce(InteractItemEvent source) {
        return Optional.of(new ItemQueryImpl(new WrappedLazyItemStack(source.getItemStack().createStack())));
    }
}
