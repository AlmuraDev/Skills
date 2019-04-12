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
package org.inspirenxe.skills.impl.skill.builtin.query;

import org.inspirenxe.skills.api.event.BlockCreationFlags;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.builtin.query.BlockTransactionEventQuery;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;

import java.util.Set;

public final class BlockTransactionQueryImpl implements BlockTransactionEventQuery {

    private final Cause cause;
    private final EventContext context;
    private final Skill skill;
    private final Transaction<BlockSnapshot> transaction;
    private final Set<BlockCreationFlags> creationFlags;

    public BlockTransactionQueryImpl(final Cause cause, final EventContext context, final Skill skill, final Transaction<BlockSnapshot> transaction,
        final Set<BlockCreationFlags> creationFlags) {
        this.cause = cause;
        this.context = context;
        this.skill = skill;
        this.transaction = transaction;
        this.creationFlags = creationFlags;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    @Override
    public EventContext getContext() {
        return this.context;
    }

    @Override
    public Skill getSkill() {
        return this.skill;
    }

    @Override
    public Transaction<BlockSnapshot> getTransaction() {
        return this.transaction;
    }

    @Override
    public Set<BlockCreationFlags> getCreationFlags() {
        return this.creationFlags;
    }
}
