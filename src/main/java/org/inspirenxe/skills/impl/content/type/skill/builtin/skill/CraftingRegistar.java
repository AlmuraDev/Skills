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
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.ItemChain;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.ItemTypes;

public final class CraftingRegistar {

    @Inject
    private static GameRegistry registry;

    @Inject
    private static BuiltinEventListener listener;

    public static void configure() {
        final SkillType type = registry.getType(SkillType.class, SkillsImpl.ID + ":crafting").orElse(null);

        if (type == null) {
            return;
        }

        // Craft
        final ItemChain craftChain = new ItemChain().matchTypeOnly().denyLevelRequired(CommonRegistar.createDenyAction("craft"));

        listener
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_PICKAXE).xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_PICKAXE).level(10).xp(10.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.IRON_PICKAXE).level(20).xp(20.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.GOLDEN_PICKAXE).level(30).xp(30.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DIAMOND_PICKAXE).level(40).xp(40.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().inverseQuery().xp(1.0));

        // Messages (Xp change/Level change
        listener
            .addMessageChain(Event.class, type, CommonRegistar.XP_TO_ACTION_BAR)
            .addMessageChain(Event.class, type, CommonRegistar.LEVEL_UP_TO_CHAT);

        // Effects (Xp change/Level change)
        listener
            .addEffectChain(Event.class, type, CommonRegistar.createFireworkEffect(SkillsImpl.ID + ":firework/crafting-level-up"));
    }

    private CraftingRegistar() {
    }
}
