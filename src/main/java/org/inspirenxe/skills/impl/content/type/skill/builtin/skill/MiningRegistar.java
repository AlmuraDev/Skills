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
package org.inspirenxe.skills.impl.content.type.skill.builtin.skill;

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

public final class MiningRegistar {

    @Inject
    private static GameRegistry registry;

    @Inject
    private static BuiltinEventListener listener;

    public static void configure() {
        final SkillType type = registry.getType(SkillType.class, SkillsImpl.ID + ":mining").orElse(null);

        if (type == null) {
            return;
        }

        // Pickaxes
        final ItemChain interactChain = new ItemChain().matchTypeOnly().denyLevelRequired(CommonRegistrar.createDenyAction("use"));

        listener
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.STONE_PICKAXE).level(10))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.IRON_PICKAXE).level(20))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.GOLDEN_PICKAXE).level(30))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.DIAMOND_PICKAXE).level(40));

        // Ores/etc
        final BlockChain breakChain = new BlockChain().matchTypeOnly().creator(CommonRegistrar.CREATOR_NONE).denyLevelRequired(CommonRegistrar.createDenyAction("break"));

        listener
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.STONE).xp(50.0).economy(0.1))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.COAL_ORE).level(10).xp(10.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.IRON_ORE).level(20).xp(20.0).economy(5.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.GOLD_ORE).level(30).xp(40.0).economy(10.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.DIAMOND_ORE).level(40).xp(200.0).economy(200.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.OBSIDIAN).level(50).xp(100.0).economy(100.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.NETHERRACK).level(60).xp(25.0).economy(25.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.END_STONE).level(70).xp(50.0).economy(50.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.EMERALD_ORE).level(80).xp(1000.0).economy(200.0));

        // Messages (Xp change/Level change)
        listener
            .addMessageChain(Event.class, type, CommonRegistrar.XP_TO_ACTION_BAR)
            .addMessageChain(Event.class, type, CommonRegistrar.LEVEL_UP_TO_CHAT);

        // Effects (Xp change/Level change)
        listener
            .addEffectChain(Event.class, type, CommonRegistrar.createFireworkEffect(SkillsImpl.ID + ":firework/mining-level-up"));
    }

    private MiningRegistar() {
    }
}
