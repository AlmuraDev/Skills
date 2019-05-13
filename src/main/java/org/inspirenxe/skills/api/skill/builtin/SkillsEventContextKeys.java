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
package org.inspirenxe.skills.api.skill.builtin;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Direction;

public final class SkillsEventContextKeys {

    public static final EventContextKey<DataHolder> DATA_HOLDER_USING_SKILL = EventContextKey.builder(DataHolder.class).id("data_holder_using_skill").name("Data Holder Using Skill").build();

    public static final EventContextKey<BlockSnapshot> PROCESSING_BLOCK = EventContextKey.builder(BlockSnapshot.class).id("processing_block").name("Processing Block").build();

    public static final EventContextKey<Direction> PROCESSING_BLOCK_SIDE = EventContextKey.builder(Direction.class).id("processing_block_side").name("Processing Block Side").build();

    public static final EventContextKey<BlockCreationTracker> BLOCK_CREATION_TRACKER = EventContextKey.builder(BlockCreationTracker.class).id("block_creation_tracker").name("Block Creation Tracker").build();

    public static final EventContextKey<ItemStackSnapshot> PROCESSING_ITEM = EventContextKey.builder(ItemStackSnapshot.class).id("processing_item").name("Processing Item").build();

    public static final EventContextKey<Entity> PROCESSING_ENTITY = EventContextKey.builder(Entity.class).id("processing_entity").name("Processing Entity").build();

    public static final EventContextKey<Player> PROCESSING_PLAYER = EventContextKey.builder(Player.class).id("processing_player").name("Processing Player").build();


}
