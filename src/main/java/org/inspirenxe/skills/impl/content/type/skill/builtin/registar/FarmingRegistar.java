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
import org.spongepowered.api.block.trait.IntegerTraits;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;

public final class FarmingRegistar {

  @Inject
  private static GameRegistry registry;
  @Inject
  private static BuiltinEventListener listener;

  private FarmingRegistar() {
  }

  // @formatter:off
  public static void configure() {
    final SkillType type = registry.getType(SkillType.class, SkillsImpl.ID + ":farming").orElse(null);

    if (type == null) {
      return;
    }

    // Hoes
    final ItemChain interactChain = new ItemChain().matchTypeOnly().denyLevelRequired(CommonRegistar.createDenyAction("use"));

    listener

      // Vanilla Tools
      .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.STONE_HOE).level(10))
      .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.IRON_HOE).level(20))
      .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.GOLDEN_HOE).level(30))
      .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.DIAMOND_HOE).level(40))

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
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/alfalfa").level(20).xp(1.0).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/corn").level(21).xp(1.1).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/barley").level(22).xp(1.20).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/bellpepper").level(23).xp(1.30).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cucumber").level(24).xp(1.4).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/lettuce").level(25).xp(1.5).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/oat").level(26).xp(1.6).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/radish").level(27).xp(1.7).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sugarbeet").level(28).xp(1.8).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/turnip").level(29).xp(1.9).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/tomato").level(30).xp(2.0).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/pea").level(31).xp(2.1).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/oregano").level(32).xp(2.2).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/olive").level(33).xp(2.3).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/mint").level(34).xp(2.4).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/onion").level(35).xp(2.5).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/bambooshoot").level(36).xp(2.6).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/basil").level(37).xp(2.7).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/chilipepper").level(38).xp(2.8).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/chive").level(39).xp(2.9).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cabbage").level(40).xp(3.0).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cumin").level(41).xp(3.1).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/garlic").level(42).xp(3.2).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/honeydew").level(43).xp(3.3).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/luffa").level(44).xp(3.4).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/dill").level(45).xp(3.5).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cotton").level(46).xp(3.6).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/coffee_bean").level(47).xp(3.7).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/celery").level(48).xp(3.8).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/broccoli").level(49).xp(3.9).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/concord_grape").level(50).xp(4.0).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cranberry").level(51).xp(4.1).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/daikon").level(52).xp(4.2).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/eggplant").level(53).xp(4.3).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/laurel").level(54).xp(4.4).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/parsley").level(55).xp(4.5).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/rosemary").level(56).xp(4.6).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sorghum").level(57).xp(4.7).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/spinach").level(58).xp(4.8).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/strawberry").level(59).xp(4.9).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/soybean").level(60).xp(5.0).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/tarragon").level(61).xp(5.1).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/white_grape").level(62).xp(5.2).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/raspberry").level(63).xp(5.3).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/pineapple").level(64).xp(5.4).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/peanut").level(65).xp(5.5).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/peppercorn").level(66).xp(5.6).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/hop").level(67).xp(5.7).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/lotusroot").level(68).xp(5.8).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/leek").level(69).xp(5.9).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/blackberry").level(70).xp(6.0).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/blueberry").level(71).xp(6.1).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/agave").level(72).xp(6.2).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/blackroot").level(73).xp(6.3).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/butterbean").level(74).xp(6.4).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/cilantro").level(75).xp(6.5).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/clove").level(76).xp(6.6).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/rice").level(77).xp(6.7).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/rye").level(78).xp(6.8).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sesame").level(79).xp(6.9).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/fargreen").level(80).xp(7.0).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sweetpepper").level(81).xp(7.1).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/thyme").level(82).xp(7.2).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/sweetpotato").level(83).xp(7.3).economy(1.0))
      .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query("almura:crop/tobacco").level(84).xp(7.4).economy(1.0))
    ;

    // Break crops
    final BlockChain breakChain = new BlockChain().matchTypeOnly().creator(CommonRegistar.CREATOR_OR_NONE).denyLevelRequired(CommonRegistar.createDenyAction("break"));
    final BlockChain breakChainNoOwner = new BlockChain().matchTypeOnly().creator(CommonRegistar.CREATOR_NONE).denyLevelRequired(CommonRegistar.createDenyAction("break"));

    listener
      // vanilla crops
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(BlockTypes.WHEAT.getDefaultState().withTrait(IntegerTraits.WHEAT_AGE, "7").orElse(null)).xp(1.0).economy(0.25))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(BlockTypes.CARROTS.getDefaultState().withTrait(IntegerTraits.CARROTS_AGE, "7").orElse(null)).level(5).xp(1.0).economy(0.25))

      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.BROWN_MUSHROOM_BLOCK).level(2))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.BROWN_MUSHROOM).level(2))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.RED_MUSHROOM_BLOCK).level(3))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.RED_MUSHROOM).level(3))

      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.MELON_BLOCK).level(7))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.PUMPKIN).level(7))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.REEDS).level(10))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.NETHER_WART_BLOCK).level(12))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChainNoOwner).query(BlockTypes.NETHER_WART).level(12))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(BlockTypes.BEETROOTS.getDefaultState().withTrait(IntegerTraits.BEETROOT_AGE, "3").orElse(null)).level(13).xp(1.0).economy(0.25))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(BlockTypes.COCOA.getDefaultState().withTrait(IntegerTraits.COCOA_AGE, "2").orElse(null)).level(15).xp(1.0).economy(0.25))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).fuzzyMatch().query(BlockTypes.POTATOES.getDefaultState().withTrait(IntegerTraits.POTATOES_AGE, "7").orElse(null)).level(17).xp(1.0).economy(0.25))

      // almura crops
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/alfalfa").level(20))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/corn").level(21))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/barley").level(22))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/bellpepper").level(23))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cucumber").level(24))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/lettuce").level(25))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/oat").level(26))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/radish").level(27))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sugarbeet").level(28))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/turnip").level(29))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/tomato").level(30))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/pea").level(31))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/oregano").level(32))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/olive").level(33))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/mint").level(34))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/onion").level(35))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/bambooshoot").level(36))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/basil").level(37))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/chilipepper").level(38))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/chive").level(39))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cabbage").level(40))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cumin").level(41))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/garlic").level(42))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/honeydew").level(43))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/luffa").level(44))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/dill").level(45))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cotton").level(46))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/coffee_bean").level(47))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/celery").level(48))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/broccoli").level(49))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/concord_grape").level(50))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cranberry").level(51))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/daikon").level(52))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/eggplant").level(53))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/laurel").level(54))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/parsley").level(55))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/rosemary").level(56))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sorghum").level(57))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/spinach").level(58))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/strawberry").level(59))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/soybean").level(60))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/tarragon").level(61))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/white_grape").level(62))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/raspberry").level(63))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/pineapple").level(64))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/peanut").level(65))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/peppercorn").level(66))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/hop").level(67))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/lotusroot").level(68))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/leek").level(69))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/blackberry").level(70))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/blueberry").level(71))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/agave").level(72))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/blackroot").level(73))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/butterbean").level(74))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/cilantro").level(75))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/clove").level(76))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/rice").level(77))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/rye").level(78))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sesame").level(79))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/fargreen").level(80))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sweetpepper").level(81))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/thyme").level(82))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/sweetpotato").level(83))
      .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query("almura:crop/tobacco").level(84))
    ;

    // Harvest crops
    final ItemChain dropsChain = new ItemChain().matchTypeOnly();

    listener
      // vanilla crop drops
      // Note:  don't check for drops, can't be trusted.

      // almura crop drops
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/crop/alfalfa_item").xp(1.0).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/corn").xp(1.1).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/barley").xp(1.2).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/bellpepper").xp(1.3).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/cucumber").xp(1.4).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/lettuce").xp(1.5).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/oat").xp(1.6).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/radish").xp(1.7).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/sugarbeet").xp(1.8).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/turnip").xp(1.9).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/tomato").xp(2.0).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/pea").xp(2.1).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/oregano").xp(2.2).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/olive").xp(2.3).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/mint").xp(2.4).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/onion").xp(2.5).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/bambooshoot").xp(2.6).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/basil").xp(2.7).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/chilipepper").xp(2.8).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/chives").xp(2.9).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/cabbage").xp(3.0).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/cumin").xp(3.1).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/garlic").xp(3.2).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/honeydew").xp(3.3).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/luffa").xp(3.4).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/dill").xp(3.5).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/cotton").xp(3.6).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/coffeebean").xp(3.7).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/celery").xp(3.8).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/broccoli").xp(3.9).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/concord_grape").xp(4.0).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/cranberry").xp(4.1).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/daikon").xp(4.2).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/eggplant").xp(4.3).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/laurel").xp(4.4).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/parsley").xp(4.5).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/rosemary").xp(4.6).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/sorghum").xp(4.7).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/spinach").xp(4.8).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/strawberry").xp(4.9).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/soybean").xp(5.0).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/tarragon").xp(5.1).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/white_grape").xp(5.2).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/raspberry").xp(5.3).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/pineapple").xp(5.4).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/peanut").xp(5.5).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/peppercorn").xp(5.6).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/hops").xp(5.7).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/lotusroot").xp(5.8).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/leek").xp(5.9).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/blackberry").xp(6.0).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/blueberry").xp(6.1).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/materials_agaveplant").xp(6.2).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/blackroot").xp(6.3).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/butterbean").xp(6.4).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/cilantro").xp(6.5).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/clove").xp(6.6).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/rice").xp(6.7).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/rye").xp(6.8).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/sesameseed").xp(6.9).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/fargreen").xp(7.0).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/sweetpepper").xp(7.1).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/thyme").xp(7.2).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:food/food/sweetpotato").xp(7.3).economy(0.5))
      .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query("almura:normal/ingredient/tobacco").xp(7.4).economy(0.5))
    ;

    // Messages (Xp change/Level change
    listener
      .addMessageChain(Event.class, type, CommonRegistar.XP_TO_ACTION_BAR)
      .addMessageChain(Event.class, type, CommonRegistar.LEVEL_UP_TO_CHAT)
    ;

    // Effects (Xp change/Level change)
    listener
      .addEffectChain(Event.class, type, CommonRegistar.createFireworkEffect(SkillsImpl.ID + ":firework/farming-level-up"))
    ;
  }
  // @formatter:on
}
