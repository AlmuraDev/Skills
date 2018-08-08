package org.inspirenxe.skills.impl.content.component.filter.owner;

import org.inspirenxe.skills.impl.content.component.filter.FilterUtils;
import org.inspirenxe.skills.impl.content.component.query.EventFilterQueryProducer;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.Optional;

public class BlockSnapshotOwnerQueryProducer implements EventFilterQueryProducer<ChangeBlockEvent, OwnerQuery> {

    @Override
    public Class<OwnerQuery> getFilterQueryType() {
        return OwnerQuery.class;
    }

    @Override
    public Class<ChangeBlockEvent> getEventType() {
        return ChangeBlockEvent.class;
    }

    @Override
    public Optional<OwnerQuery> produce(ChangeBlockEvent source) {
        FilterUtils.checkChangeBlockEvent(source);
        // TODO - Use a BlockTransactionSource to control where the owner comes from
        return source.getTransactions().get(0).getOriginal().getCreator().map(OwnerQueryImpl::new);
    }
}
