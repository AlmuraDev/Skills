/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.inspirenxe.skills.impl.content.type.skill.component.event.flatten;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ChangeBlockEventFlattener implements EventFlattener<ChangeBlockEvent> {

    @Override
    public Collection<ChangeBlockEvent> flatten(final ChangeBlockEvent event) {
        if (event.getTransactions().size() == 1) {
            return ImmutableList.of(event);
        }
        final ImmutableList.Builder<ChangeBlockEvent> flattened = ImmutableList.builder();
        final Function<List<Transaction<BlockSnapshot>>, ChangeBlockEvent> creationFun = this.getEventConstructor(event);

        for (final Transaction<BlockSnapshot> transaction: event.getTransactions()) {
            flattened.add(creationFun.apply(ImmutableList.of(transaction)));
        }
        return flattened.build();
    }

    private Function<List<Transaction<BlockSnapshot>>, ChangeBlockEvent> getEventConstructor(final ChangeBlockEvent event) {
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
