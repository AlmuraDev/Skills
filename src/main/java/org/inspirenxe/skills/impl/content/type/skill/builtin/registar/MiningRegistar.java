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

import java.util.Arrays;

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
        final ItemChain interactChain = new ItemChain().matchTypeOnly().denyLevelRequired(CommonRegistar.createDenyAction("use"));

        listener
            // Vanilla Tools
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.STONE_PICKAXE).level(5))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.IRON_PICKAXE).level(10))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.GOLDEN_PICKAXE).level(20))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.DIAMOND_PICKAXE).level(30))

            // Mods
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query("tconstruct:pickaxe").level(50))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query("tconstruct:hammer").level(50))
            ;

        // Ores/etc
        final BlockChain breakChain = new BlockChain().matchTypeOnly().creator(CommonRegistar.CREATOR_NONE).denyLevelRequired(CommonRegistar.createDenyAction("break"));
        final BlockState stoneBrick = BlockTypes.STONEBRICK.getDefaultState();

        listener
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.STONE).xp(2.5).economy(0.1))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.NETHERRACK).xp(2.5).economy(0.1))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.SANDSTONE).level(10).xp(4.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.RED_SANDSTONE).level(10).xp(4.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.COAL_ORE).level(15).xp(8.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.IRON_ORE).level(20).xp(15.0).economy(2.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.END_STONE).level(20).xp(5.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.HARDENED_CLAY).level(20).xp(7.0).economy(0.35))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.STAINED_HARDENED_CLAY).level(20).xp(7.0).economy(0.35))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.LAPIS_ORE).level(25).xp(15.0).economy(5.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.GLOWSTONE).level(25).xp(6.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.QUARTZ_ORE).level(27).xp(20.0).economy(3.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.GOLD_ORE).level(30).xp(25.0).economy(5.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.PRISMARINE).level(30).xp(8.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.REDSTONE_ORE).level(35).xp(30.0).economy(5.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.LIT_REDSTONE_ORE).level(35).xp(30.0).economy(5.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.DIAMOND_ORE).level(40).xp(35.0).economy(10.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.OBSIDIAN).level(50).xp(50.0).economy(10.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.EMERALD_ORE).level(80).xp(40.0).economy(20.0))

            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(stoneBrick.withTrait(EnumTraits.STONEBRICK_VARIANT, "mossy_stonebrick").orElse(null)).level(15).xp(7.0).economy(0.5))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(stoneBrick.withTrait(EnumTraits.STONEBRICK_VARIANT, "stonebrick").orElse(null)).level(15).xp(4.0).economy(0.5))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(stoneBrick.withTrait(EnumTraits.STONEBRICK_VARIANT, "cracked_stonebrick").orElse(null)).level(15).xp(5.0).economy(0.5))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(stoneBrick.withTrait(EnumTraits.STONEBRICK_VARIANT, "chiseled_stonebrick").orElse(null)).level(15).xp(5.0).economy(0.5))

            // Mod Blocks (ores only: machines, cables, rubber wood etc. must not be gated or paid by Mining)

            // IC2 (1.12): the ores are variants of ic2:resource (property "type"), a block that also holds metal blocks, machine casings and
            // reinforced stone, so only the ore states are queried. Without IC2 (or if the variant names differ) these match nothing.
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith("ic2:resource", "type", "copper_ore", "tin_ore")).level(20).xp(15.0).economy(2.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith("ic2:resource", "type", "lead_ore")).level(30).xp(22.0).economy(4.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith("ic2:resource", "type", "uranium_ore")).level(45).xp(38.0).economy(8.0))

            // Railcraft (1.12): ore blocks only. railcraft:ore (property "variant") mixes common and gem ores, so the gem variants are queried
            // exactly first; the type-only railcraft:ore rule after them catches the remaining variants (sulfur, saltpeter).
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith("railcraft:ore", "variant", "dark_lapis")).level(25).xp(15.0).economy(5.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith("railcraft:ore", "variant", "dark_diamond")).level(40).xp(35.0).economy(10.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith("railcraft:ore", "variant", "dark_emerald")).level(80).xp(40.0).economy(20.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("railcraft:ore").level(20).xp(12.0).economy(2.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("railcraft:ore_metal_poor").level(15).xp(10.0).economy(1.5))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("railcraft:ore_metal").level(20).xp(15.0).economy(2.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("railcraft:ore_magic").level(60).xp(40.0).economy(8.0))

            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("sgcraft:naquadahore").level(60).xp(50.0).economy(8.0))

            // Almura
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:horizontal/ore/saltore").level(20).xp(15.0).economy(2.0))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:horizontal/ore/marbleore").level(30).xp(22.0).economy(2.0))
            ;
        // Messages (Xp change/Level change)
        listener
            .addMessageChain(Event.class, type, CommonRegistar.XP_TO_ACTION_BAR)
            .addMessageChain(Event.class, type, CommonRegistar.LEVEL_UP_TO_CHAT);

        // Effects (Xp change/Level change)
        listener
            .addEffectChain(Event.class, type, CommonRegistar.createFireworkEffect(SkillsImpl.ID + ":firework/mining-level-up"));
    }

    /**
     * Every state of the block {@code id} whose trait named {@code traitName} has one of the given values (compared case-insensitively by
     * name). Returns no states if the block (e.g. its mod is not installed) or the trait doesn't exist.
     */
    private static BlockState[] statesWith(final String id, final String traitName, final String... values) {
        final BlockType blockType = registry.getType(BlockType.class, id).orElse(null);
        if (blockType == null) {
            return new BlockState[0];
        }
        final BlockTrait<?> trait = blockType.getTrait(traitName).orElse(null);
        if (trait == null) {
            return new BlockState[0];
        }
        return blockType.getAllBlockStates().stream()
            .filter(state -> state.getTraitValue(trait).map(value -> Arrays.stream(values).anyMatch(value.toString()::equalsIgnoreCase)).orElse(false))
            .toArray(BlockState[]::new);
    }

    private MiningRegistar() {
    }
}
