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
package org.inspirenxe.skills.impl.content.type.skill.builtin.registar;

import com.google.inject.Inject;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.content.type.skill.builtin.BuiltinEventListener;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.BlockChain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.ItemChain;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;

public final class DiggerRegistar {

    @Inject
    private static GameRegistry registry;

    @Inject
    private static BuiltinEventListener listener;

    public static void configure() {
        final SkillType type = registry.getType(SkillType.class, SkillsImpl.ID + ":digger").orElse(null);

        if (type == null) {
            return;
        }

        // Shovels
        final ItemChain interactChain = new ItemChain().matchTypeOnly().denyLevelRequired(CommonRegistar.createDenyAction("use"));

        listener
            // Vanilla Tools
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.STONE_SHOVEL).level(5))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.IRON_SHOVEL).level(10))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.GOLDEN_SHOVEL).level(20))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.DIAMOND_SHOVEL).level(30))

            // Mods
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query("tconstruct:shovel").level(50))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query("tconstruct:excavator").level(50))
        ;



        // Dirt/etc
        final BlockChain breakChain = new BlockChain().matchTypeOnly().creator(CommonRegistar.CREATOR_NONE).denyLevelRequired(CommonRegistar.createDenyAction("break"));

        listener
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.DIRT).xp(2.5).economy(0.10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.GRASS).xp(2.5).economy(0.15))

            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.SAND).level(5).xp(5.0).economy(0.25))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.GRAVEL).level(5).xp(6.0).economy(0.35))

            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.CLAY).level(10).xp(6.0).economy(0.15))

            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.GRASS_PATH).level(20).xp(4.5).economy(0.20))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.STAINED_HARDENED_CLAY).level(20).xp(5.0).economy(0.35))

            ;

        // Messages (Xp change/Level change
        listener
            .addMessageChain(Event.class, type, CommonRegistar.XP_TO_ACTION_BAR)
            .addMessageChain(Event.class, type, CommonRegistar.LEVEL_UP_TO_CHAT);

        // Effects (Xp change/Level change)
        listener
            .addEffectChain(Event.class, type, CommonRegistar.createFireworkEffect(SkillsImpl.ID + ":firework/crafting-level-up"));
    }

    private DiggerRegistar() {
    }
}
