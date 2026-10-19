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
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.block.trait.EnumTraits;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;

public final class WoodcuttingRegistar {

    @Inject
    private static GameRegistry registry;

    @Inject
    private static BuiltinEventListener listener;

    public static void configure() {
        final SkillType type = registry.getType(SkillType.class, SkillsImpl.ID + ":woodcutting").orElse(null);

        if (type == null) {
            return;
        }

        // Axes
        final ItemChain interactChain = new ItemChain().matchTypeOnly().denyLevelRequired(CommonRegistar.createDenyAction("use"));

        listener
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.STONE_AXE).level(5))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.IRON_AXE).level(10))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.GOLDEN_AXE).level(20))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.DIAMOND_AXE).level(30))

            // Mods
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query("tconstruct:lumberaxe").level(55))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query("tconstruct:hatchet").level(55))
        ;


        // Logs/etc
        final BlockChain breakChain = new BlockChain().matchTypeOnly().creator(CommonRegistar.CREATOR_NONE).denyLevelRequired(CommonRegistar.createDenyAction("break"));

        // Vanilla logs: match every axis state (x/y/z/none) of the variant, not just the default (axis=y) state
        listener
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith(BlockTypes.LOG, EnumTraits.LOG_VARIANT, "oak")).xp(2.5).economy(0.1))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith(BlockTypes.LOG, EnumTraits.LOG_VARIANT, "spruce")).level(5).xp(3.5).economy(0.5))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith(BlockTypes.LOG, EnumTraits.LOG_VARIANT, "jungle")).level(10).xp(4.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith(BlockTypes.LOG, EnumTraits.LOG_VARIANT, "birch")).level(15).xp(4.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith(BlockTypes.LOG2, EnumTraits.LOG2_VARIANT, "dark_oak")).level(20).xp(7.0).economy(1.5))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith(BlockTypes.LOG2, EnumTraits.LOG2_VARIANT, "acacia")).level(25).xp(10.0).economy(1.5))

            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("ic2:rubber_wood").level(50).xp(15.0).economy(1.0))

            // Almura logs (type-only match already covers every axis state)
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/afledos_tree_log").level(30).xp(10.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/cherry_tree_log").level(30).xp(10.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/citrus_tree_log").level(35).xp(11.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/dark_ephedra_tree_log").level(35).xp(11.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/doku_tree_log").level(40).xp(12.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/fraze_tree_log").level(40).xp(12.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/green_apple_tree_log").level(45).xp(13.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/juko_tree_log").level(45).xp(13.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/kiwi_tree_log").level(50).xp(14.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/light_ephedra_tree_log").level(50).xp(14.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/liku_tree_log").level(55).xp(15.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/morbus_tree_log").level(55).xp(15.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/peach_tree_log").level(60).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/plum_tree_log").level(60).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:log/walnut_tree_log").level(65).xp(16.0).economy(1.0))


            ;

        // Messages (Xp change/Level change)
        listener
            .addMessageChain(Event.class, type, CommonRegistar.XP_TO_ACTION_BAR)
            .addMessageChain(Event.class, type, CommonRegistar.LEVEL_UP_TO_CHAT);

        // Effects (Xp change/Level change)
        listener
            .addEffectChain(Event.class, type, CommonRegistar.createFireworkEffect(SkillsImpl.ID + ":firework/woodcutting-level-up"));
    }

    /**
     * Every state of {@code type} whose {@code trait} has the given value, regardless of its other traits (e.g. log axis).
     */
    private static BlockState[] statesWith(final BlockType type, final BlockTrait<?> trait, final Object value) {
        return type.getAllBlockStates().stream()
            .filter(state -> state.withTrait(trait, value).map(state::equals).orElse(false))
            .toArray(BlockState[]::new);
    }

    private WoodcuttingRegistar() {
    }
}
