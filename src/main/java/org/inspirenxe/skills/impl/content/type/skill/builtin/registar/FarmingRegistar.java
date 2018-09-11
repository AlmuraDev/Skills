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
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;

public final class FarmingRegistar {

    private FarmingRegistar() {
    }

    @Inject
    private static GameRegistry registry;

    @Inject
    private static BuiltinEventListener listener;

    public static void configure() {
        final SkillType type = registry.getType(SkillType.class, SkillsImpl.ID + ":farming").orElse(null);

        if (type == null) {
            return;
        }

        // Hoes
        final ItemChain interactChain = new ItemChain().matchTypeOnly().denyLevelRequired(CommonRegistar.createDenyAction("use"));

        listener
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.STONE_HOE).level(10))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.IRON_HOE).level(20))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.GOLDEN_HOE).level(30))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.DIAMOND_HOE).level(40))
        ;

        // Bonemeal
        listener
          .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.DYE).level(20))
        ;

        // Plant crops
        final BlockChain placeChain = new BlockChain().matchTypeOnly().denyLevelRequired(CommonRegistar.createDenyAction("plant"));

        listener
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query(BlockTypes.CARROTS).level(10).xp(2.0).economy(1.0));

        // Break crops
        final BlockChain breakChain = new BlockChain().matchTypeOnly().creator(CommonRegistar.CREATOR_OR_NONE).denyLevelRequired(CommonRegistar.createDenyAction("break"));

        listener
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.CARROTS).level(10));

        // Harvest crops
        final ItemChain dropsChain = new ItemChain().matchTypeOnly();

        listener
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query(ItemTypes.WHEAT).xp(10.0).economy(1.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query(ItemTypes.CARROT).xp(20.0).economy(1.5).level(10));

        // Messages (Xp change/Level change
        listener
            .addMessageChain(Event.class, type, CommonRegistar.XP_TO_ACTION_BAR)
            .addMessageChain(Event.class, type, CommonRegistar.LEVEL_UP_TO_CHAT);

        // Effects (Xp change/Level change)
        listener
            .addEffectChain(Event.class, type, CommonRegistar.createFireworkEffect(SkillsImpl.ID + ":farming-level-up"));
    }
}
