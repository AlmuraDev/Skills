package org.inspirenxe.skills.impl.content.type.skill.component.event.flatten;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ChangeBlockEventFlattener implements EventFlattener<ChangeBlockEvent> {

    @Override
    public Collection<ChangeBlockEvent> flatten(ChangeBlockEvent event) {
        ImmutableList.Builder<ChangeBlockEvent> flattened = ImmutableList.builder();
        Function<List<Transaction<BlockSnapshot>>, ChangeBlockEvent> creationFun = this.getEventConstructor(event);

        for (Transaction<BlockSnapshot> transaction: event.getTransactions()) {
            flattened.add(creationFun.apply(ImmutableList.of(transaction)));
        }
        return flattened.build();
    }

    private Function<List<Transaction<BlockSnapshot>>, ChangeBlockEvent> getEventConstructor(ChangeBlockEvent event) {
        if (event instanceof ChangeBlockEvent.Decay) {
            return (transaction) -> SpongeEventFactory.createChangeBlockEventDecay(event.getCause(), transaction);
        } else if (event instanceof ChangeBlockEvent.Grow) {
            return (transaction -> SpongeEventFactory.createChangeBlockEventGrow(event.getCause(), transaction));
        } else if (event instanceof ChangeBlockEvent.Break) {
            return  (transaction -> SpongeEventFactory.createChangeBlockEventBreak(event.getCause(), transaction));
        } else if (event instanceof ChangeBlockEvent.Place) {
            return (transaction -> SpongeEventFactory.createChangeBlockEventPlace(event.getCause(), transaction));
        } else if (event instanceof ChangeBlockEvent.Modify) {
            return (transaction -> SpongeEventFactory.createChangeBlockEventModify(event.getCause(), transaction));
        } else if (event instanceof ChangeBlockEvent.Post) {
            return (transaction -> SpongeEventFactory.createChangeBlockEventPost(event.getCause(), transaction));
        } else {
            throw new IllegalStateException("Unknown ChangeblockEvent subclass " + event);
        }
    }
}
