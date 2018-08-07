package org.inspirenxe.skills.impl.content.component.filter.block;

import com.google.common.collect.ImmutableList;
import org.inspirenxe.skills.impl.content.component.query.EventFilterQueryProducer;
import org.inspirenxe.skills.impl.content.parser.lazy.block.BlockTransactionSource;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.Collection;

public class BlockQueryProducer implements EventFilterQueryProducer<ChangeBlockEvent, BlockQuery> {

    @Override
    public Class<ChangeBlockEvent> getEventType() {
        return ChangeBlockEvent.class;
    }

    @Override
    public Collection<BlockQuery> produce(ChangeBlockEvent event) {
        BlockTransactionSource source = BlockTransactionSource.ORIGINAL;

        if (event instanceof ChangeBlockEvent.Place) {
            source = BlockTransactionSource.FINAL;
        } else if (event instanceof ChangeBlockEvent.Break) {
            source = BlockTransactionSource.ORIGINAL;
        }
        BlockTransactionSource finalSource = source;

        return event.getTransactions().stream().map(t -> new BlockQuery(t, finalSource)).collect(ImmutableList.toImmutableList());
    }
}
