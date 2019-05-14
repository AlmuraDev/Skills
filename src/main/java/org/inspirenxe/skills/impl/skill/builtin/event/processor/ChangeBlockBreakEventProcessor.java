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

import static org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys.PROCESSING_BLOCK;
import static org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys.PROCESSING_ITEM;
import static org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys.PROCESSING_PLAYER;
import static org.spongepowered.api.event.cause.EventContextKeys.USED_ITEM;
import static org.spongepowered.api.item.inventory.ItemStackSnapshot.NONE;

import org.inspirenxe.skills.impl.SkillsImpl;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContext;

import java.util.Objects;

public final class ChangeBlockBreakEventProcessor extends AbstractBlockTransactionEventProcessor {

    public ChangeBlockBreakEventProcessor() {
        super(SkillsImpl.ID + ":change_block_break", "Change Block Break", event -> event instanceof ChangeBlockEvent.Break);
    }

    @Override
    EventContext populateTransactionContext(final Event event, final EventContext context, final Transaction<BlockSnapshot> transaction) {
        return EventContext
            .builder()
            .from(context)
            .add(PROCESSING_PLAYER, Objects.requireNonNull(event.getCause().first(Player.class).orElse(null)))
            .add(PROCESSING_ITEM, context.get(USED_ITEM).orElse(NONE))
            .add(PROCESSING_BLOCK, transaction.getOriginal())
            .build();
    }
}
