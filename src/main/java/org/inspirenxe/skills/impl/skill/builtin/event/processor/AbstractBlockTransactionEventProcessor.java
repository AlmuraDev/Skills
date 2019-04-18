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
package org.inspirenxe.skills.impl.skill.builtin.event.processor;

import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.builtin.query.EventQuery;
import org.inspirenxe.skills.impl.skill.builtin.query.EventQueryImpl;
import org.inspirenxe.skills.impl.skill.builtin.query.PlayerEventQueryImpl;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractBlockTransactionEventProcessor extends AbstractTransactionEventProcessor<BlockSnapshot> {

    AbstractBlockTransactionEventProcessor(final String id, final String name, final Predicate<Event> shouldProcess) {
        super(id, name, shouldProcess);
    }

    @Override
    public EventQuery getCancelEventQuery(final Event event, final User user, final Skill skill) {
        if (user.getPlayer().isPresent()) {
            return new PlayerEventQueryImpl(event.getCause(), event.getContext(), skill, user.getPlayer().get());
        }

        return new EventQueryImpl(event.getCause(), event.getContext(), skill);
    }

    @Override
    public List<Transaction<BlockSnapshot>> getTransactions(final Event event) {
        if (event instanceof ChangeBlockEvent) {
            return ((ChangeBlockEvent) event).getTransactions();
        }
        return Collections.emptyList();
    }
}
