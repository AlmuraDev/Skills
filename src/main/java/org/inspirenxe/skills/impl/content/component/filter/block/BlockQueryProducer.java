package org.inspirenxe.skills.impl.content.component.filter.block;

import com.google.common.collect.ImmutableList;
import org.inspirenxe.skills.impl.content.component.query.EventFilterQueryProducer;
import org.inspirenxe.skills.impl.content.parser.lazy.block.BlockTransactionSource;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.Collection;
import java.util.Optional;

public class BlockQueryProducer implements EventFilterQueryProducer<ChangeBlockEvent, BlockQuery> {

    @Override
    public Class<BlockQuery> getFilterQueryType() {
        return BlockQuery.class;
    }

    @Override
    public Class<ChangeBlockEvent> getEventType() {
        return ChangeBlockEvent.class;
    }

    @Override
    public Optional<BlockQuery> produce(ChangeBlockEvent event) {
        if (event.getTransactions().size() != 1) {
            throw new IllegalStateException("Failed to flatten event " + event);
        }

        BlockTransactionSource source = BlockTransactionSource.ORIGINAL;

        if (event instanceof ChangeBlockEvent.Place) {
            source = BlockTransactionSource.FINAL;
        } else if (event instanceof ChangeBlockEvent.Break) {
            source = BlockTransactionSource.ORIGINAL;
        }
        BlockTransactionSource finalSource = source;
        return Optional.of(new BlockQuery(event.getTransactions().get(0), finalSource));
    }
}
