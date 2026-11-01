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
import org.spongepowered.api.block.trait.IntegerTraits;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;

public final class FarmingRegistar {

    private FarmingRegistar() {
    }

    /**
     * Every state of {@code type} whose {@code trait} has the given value, regardless of its other traits (e.g. cocoa facing).
     */
    private static BlockState[] statesWith(final BlockType type, final BlockTrait<?> trait, final Object value) {
        return type.getAllBlockStates().stream()
            .filter(state -> state.withTrait(trait, value).map(state::equals).orElse(false))
            .toArray(BlockState[]::new);
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

            // Vanilla Tools
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.STONE_HOE).level(5))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.IRON_HOE).level(10))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.GOLDEN_HOE).level(20))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.DIAMOND_HOE).level(30))

            // Mod Tools
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query("tconstruct:mattock").level(60))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query("tconstruct:scythe").level(60))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query("tconstruct:kama").level(60))
            ;

        // Plant crops
        final BlockChain placeChain = new BlockChain().matchTypeOnly().denyLevelRequired(CommonRegistar.createDenyAction("plant"));

        listener
            // Farmland
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query(BlockTypes.FARMLAND).xp(0.75).economy(0.10))
            // almura crops
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sweetcorn").level(10).xp(1.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/popcorn").level(10).xp(1.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/ginger_root").level(10).xp(1.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cardamon").level(10).xp(1.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/alfalfa").level(10).xp(1.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/corn").level(10).xp(1.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/barley").level(10).xp(1.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/bellpepper").level(10).xp(1.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cucumber").level(10).xp(1.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/lettuce").level(10).xp(1.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/oat").level(10).xp(1.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/radish").level(20).xp(4.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sugarbeet").level(20).xp(4.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/turnip").level(20).xp(4.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/tomato").level(20).xp(4.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/pea").level(20).xp(4.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/oregano").level(20).xp(4.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/olive").level(20).xp(4.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sesame").level(20).xp(4.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/onion").level(20).xp(4.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/bambooshoot").level(20).xp(4.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/basil").level(20).xp(4.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/rye").level(30).xp(7.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/thyme").level(30).xp(7.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/chilipepper").level(30).xp(7.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/chive").level(30).xp(7.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cabbage").level(30).xp(7.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cumin").level(30).xp(7.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/garlic").level(30).xp(7.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/mint").level(30).xp(7.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/luffa").level(30).xp(7.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/dill").level(30).xp(7.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cotton").level(30).xp(7.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/celery").level(40).xp(10.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/broccoli").level(40).xp(10.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/daikon").level(40).xp(10.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/eggplant").level(40).xp(10.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/laurel").level(40).xp(10.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/parsley").level(40).xp(10.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/rosemary").level(40).xp(10.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sorghum").level(40).xp(10.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/spinach").level(40).xp(10.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/soybean").level(40).xp(10.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/tarragon").level(40).xp(10.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/peanut").level(50).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/peppercorn").level(50).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/hop").level(50).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/lotusroot").level(50).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/leek").level(50).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/agave").level(50).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/blackroot").level(50).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/butterbean").level(50).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cilantro").level(50).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/clove").level(50).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/rice").level(50).xp(15.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/honeydew").level(60).xp(24.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/concord_grape").level(60).xp(24.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cranberry").level(60).xp(24.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/blackberry").level(60).xp(24.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/blueberry").level(60).xp(24.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/strawberry").level(60).xp(24.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/coffee_bean").level(60).xp(24.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/fargreen").level(60).xp(24.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sweetpepper").level(60).xp(24.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/white_grape").level(60).xp(24.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/raspberry").level(70).xp(33.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/pineapple").level(70).xp(33.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/banana").level(70).xp(33.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sweetpotato").level(70).xp(33.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/tobacco").level(70).xp(33.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/vanilla_bean").level(70).xp(33.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cantaloupe").level(70).xp(33.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cinnamon").level(70).xp(33.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sage").level(70).xp(33.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/kidneybean").level(70).xp(33.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/pintobean").level(80).xp(42.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/anise").level(80).xp(42.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sweetredbean").level(80).xp(42.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/seaweed").level(80).xp(42.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/juniper").level(80).xp(42.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/saffron").level(80).xp(42.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/yummyberry").level(80).xp(42.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/dragonfruit").level(80).xp(42.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/mooncheese").level(80).xp(42.5).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/wasabi").level(80).xp(42.5).economy(1.0))
            ;

        // Break crops
        final BlockChain breakChain = new BlockChain().matchTypeOnly().creator(CommonRegistar.CREATOR_OR_ANY).denyLevelRequired(CommonRegistar.createDenyAction("break"));
        final BlockChain breakChainNoOwner = new BlockChain().matchTypeOnly().creator(CommonRegistar.CREATOR_NONE).denyLevelRequired(CommonRegistar.createDenyAction("break"));

        listener
            // vanilla crops
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(BlockTypes.WHEAT.getDefaultState().withTrait(IntegerTraits.WHEAT_AGE, "7").orElse(null)).xp(1.0).economy(0.25))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(BlockTypes.CARROTS.getDefaultState().withTrait(IntegerTraits.CARROTS_AGE, "7").orElse(null)).level(5).xp(1.0).economy(0.25))

            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.BROWN_MUSHROOM_BLOCK).level(2).xp(2.0).economy(0.10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.BROWN_MUSHROOM).level(2).xp(2.0).economy(0.10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.RED_MUSHROOM_BLOCK).level(3).xp(2.0).economy(0.10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.RED_MUSHROOM).level(3).xp(2.0).economy(0.10))

            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.REEDS).level(3).xp(2.0).economy(0.10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.MELON_BLOCK).level(7).xp(3.0).economy(0.25))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.PUMPKIN).level(7).xp(3.0).economy(0.25))
            // Nether wart crop: mature (age 3) pays, any other age is only gated
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith(BlockTypes.NETHER_WART, IntegerTraits.NETHER_WART_AGE, "3")).level(8).xp(4.0).economy(0.25))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.NETHER_WART).level(8))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(BlockTypes.BEETROOTS.getDefaultState().withTrait(IntegerTraits.BEETROOT_AGE, "3").orElse(null)).level(8).xp(1.0).economy(0.25))
            // Cocoa: mature (age 2) pods on every facing
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(statesWith(BlockTypes.COCOA, IntegerTraits.COCOA_AGE, "2")).level(9).xp(1.0).economy(0.25))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(BlockTypes.POTATOES.getDefaultState().withTrait(IntegerTraits.POTATOES_AGE, "7").orElse(null)).level(9).xp(1.0).economy(0.25))

            // almura crops
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sweetcorn").level(10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/popcorn").level(10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/ginger_root").level(10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cardamon").level(10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/alfalfa").level(10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/corn").level(10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/barley").level(10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/bellpepper").level(10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cucumber").level(10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/lettuce").level(10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/oat").level(10))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/radish").level(20))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sugarbeet").level(20))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/turnip").level(20))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/tomato").level(20))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/pea").level(20))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/oregano").level(20))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/olive").level(20))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sesame").level(20))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/onion").level(20))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/bambooshoot").level(20))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/basil").level(20))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/rye").level(30))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/thyme").level(30))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/chilipepper").level(30))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/chive").level(30))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cabbage").level(30))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cumin").level(30))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/garlic").level(30))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/mint").level(30))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/luffa").level(30))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/dill").level(30))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cotton").level(30))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/celery").level(40))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/broccoli").level(40))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/daikon").level(40))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/eggplant").level(40))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/laurel").level(40))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/parsley").level(40))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/rosemary").level(40))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sorghum").level(40))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/spinach").level(40))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/soybean").level(40))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/tarragon").level(40))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/peanut").level(50))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/peppercorn").level(50))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/hop").level(50))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/lotusroot").level(50))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/leek").level(50))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/agave").level(50))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/blackroot").level(50))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/butterbean").level(50))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cilantro").level(50))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/clove").level(50))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/rice").level(50))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/honeydew").level(60))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/concord_grape").level(60))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cranberry").level(60))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/blackberry").level(60))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/blueberry").level(60))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/strawberry").level(60))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/coffee_bean").level(60))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/fargreen").level(60))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sweetpepper").level(60))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/white_grape").level(60))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/raspberry").level(70))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/pineapple").level(70))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/banana").level(70))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sweetpotato").level(70))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/tobacco").level(70))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/vanilla_bean").level(70))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cantaloupe").level(70))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cinnamon").level(70))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sage").level(70))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/kidneybean").level(70))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/pintobean").level(80))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/anise").level(80))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sweetredbean").level(80))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/seaweed").level(80))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/juniper").level(80))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/saffron").level(80))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/yummyberry").level(80))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/dragonfruit").level(80))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/mooncheese").level(80))
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/wasabi").level(80))
        ;

        // Harvest crops
        final ItemChain dropsChain = new ItemChain().matchTypeOnly();

        listener
            // vanilla crop drops
            // Note:  don't check for drops, can't be trusted.

            // almura crop drops
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/sweetcorn").xp(1.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/popcorn_cob").xp(1.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/ginger").xp(1.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/cardamon").xp(1.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/crop/alfalfa_item").xp(1.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/corn").xp(1.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/barley").xp(1.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/bellpepper").xp(1.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/cucumber").xp(1.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/lettuce").xp(1.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/oat").xp(1.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/radish").xp(4.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/sugarbeet").xp(4.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/turnip").xp(4.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/tomato").xp(4.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/pea").xp(4.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/oregano").xp(4.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/olive").xp(4.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/sesameseed").xp(4.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/onion").xp(4.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/bambooshoot").xp(4.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/basil").xp(4.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/rye").xp(7.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/thyme").xp(7.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/chilipepper").xp(7.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/chives").xp(7.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/cabbage").xp(7.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/cumin").xp(7.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/garlic").xp(7.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/mint").xp(7.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/luffa").xp(7.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/dill").xp(7.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/cotton").xp(7.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/celery").xp(10.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/broccoli").xp(10.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/daikon").xp(10.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/eggplant").xp(10.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/laurel").xp(10.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/parsley").xp(10.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/rosemary").xp(10.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/sorghum").xp(10.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/spinach").xp(10.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/soybean").xp(10.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/tarragon").xp(10.0).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/peanut").xp(15.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/peppercorn").xp(15.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/hops").xp(15.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/lotusroot").xp(15.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/leek").xp(15.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/materials_agaveplant").xp(15.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/blackroot").xp(15.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/butterbean").xp(15.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/cilantro").xp(15.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/clove").xp(15.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/rice").xp(15.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/honeydew").xp(24.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/concord_grape").xp(24.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/cranberry").xp(24.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/blackberry").xp(24.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/blueberry").xp(24.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/strawberry").xp(24.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/coffeebean").xp(24.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/fargreen").xp(24.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/sweetpepper").xp(24.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/white_grape").xp(24.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/raspberry").xp(33.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/pineapple").xp(33.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/banana").xp(33.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/sweetpotato").xp(33.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/tobacco").xp(33.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/vanilla").xp(33.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/cantaloupe").xp(33.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/cinnamon").xp(33.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/sage").xp(33.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/kidneybean").xp(33.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/pintobean").xp(42.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/aniseseed").xp(42.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/sweetredbean").xp(42.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/seaweed").xp(42.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/materials_juniperberry").xp(42.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/saffron").xp(42.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/yummyberry").xp(42.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/dragonfruit").xp(42.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/mooncheese").xp(42.5).economy(0.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/wasabi").xp(42.5).economy(0.5));

        // Messages (Xp change/Level change
        listener
            .addMessageChain(Event.class, type, CommonRegistar.XP_TO_ACTION_BAR)
            .addMessageChain(Event.class, type, CommonRegistar.LEVEL_UP_TO_CHAT);

        // Effects (Xp change/Level change)
        listener
            .addEffectChain(Event.class, type, CommonRegistar.createFireworkEffect(SkillsImpl.ID + ":firework/farming-level-up"));
    }
}
