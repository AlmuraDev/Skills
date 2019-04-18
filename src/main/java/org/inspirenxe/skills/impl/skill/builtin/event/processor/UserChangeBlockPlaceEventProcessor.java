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

import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.event.BlockCreationFlags;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.builtin.query.EventQuery;
import org.inspirenxe.skills.impl.skill.builtin.query.BlockTransactionQueryImpl;
import org.inspirenxe.skills.impl.skill.builtin.query.PlayerBlockTransactionQueryImpl;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;

import java.util.Set;
import java.util.function.Predicate;

public final class UserChangeBlockPlaceEventProcessor extends AbstractBlockTransactionEventProcessor {

    public UserChangeBlockPlaceEventProcessor(final String id, final String name, final Predicate<Event> shouldProcess) {
        super(id, name, shouldProcess);
    }

    @Override
    public EventQuery getCancelTransactionQuery(final Event event, final User user, final SkillService service, final Skill skill,
        final Transaction<BlockSnapshot> transaction) {

        final Set<BlockCreationFlags> flags = service.getBlockCreationTracker().getCreationFlags(transaction.getFinal());

        if (user.getPlayer().isPresent()) {
            return new PlayerBlockTransactionQueryImpl(event.getCause(), event.getContext(), user.getPlayer().get(), skill, transaction,
                flags, false);
        }

        return new BlockTransactionQueryImpl(event.getCause(), event.getContext(), skill, transaction, flags, false);
    }
}
