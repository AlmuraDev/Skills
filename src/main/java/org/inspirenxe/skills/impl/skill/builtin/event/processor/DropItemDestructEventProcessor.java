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

import org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.item.inventory.DropItemEvent;

import java.util.List;

public final class DropItemDestructEventProcessor extends AbstractEntityEventProcessor {

    public DropItemDestructEventProcessor() {
        super(SkillsImpl.ID + ":drop_item_destruct", "Drop Item Destruct", event -> event instanceof DropItemEvent.Destruct);
    }

    @Override
    List<Entity> getEntities(final Event event) {
        return ((DropItemEvent.Destruct) event).getEntities();
    }

    @Override
    EventContext populateEntityContext(final Event event, final EventContext context, final Entity entity) {
        if (!(entity instanceof Item)) {
            return super.populateEntityContext(event, context, entity);
        }

        System.err.println(event);

        return EventContext
            .builder()
            .from(context)
            .add(SkillsEventContextKeys.PROCESSING_BLOCK, event.getCause().first(BlockSnapshot.class).orElse(BlockSnapshot.NONE))
            .add(SkillsEventContextKeys.PROCESSING_ITEM, ((Item) entity).getItemData().item().get())
            .add(SkillsEventContextKeys.PROCESSING_ENTITY, entity)
            .build();
    }

    @Override
    void cancelEntity(final Event event, final Entity entity) {
        ((DropItemEvent.Destruct) event).filterEntities(e -> e == entity);
    }
}
