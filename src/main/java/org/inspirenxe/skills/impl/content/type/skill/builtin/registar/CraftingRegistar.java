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
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.ItemTypes;

public final class CraftingRegistar {

  @Inject
  private static GameRegistry registry;

  @Inject
  private static BuiltinEventListener listener;

  private CraftingRegistar() {
  }

  // @formatter:off
  public static void configure() {
    final SkillType type = registry.getType(SkillType.class, SkillsImpl.ID + ":crafting").orElse(null);

    if (type == null) {
      return;
    }

    // Craft
    final ItemChain craftChain = new ItemChain().matchTypeOnly().denyLevelRequired(CommonRegistar.createDenyAction("craft"));

    listener
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.PLANKS).xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_SLAB).xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.BED).xp(15.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONEBRICK).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_SLAB).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_SLAB2).xp(2.0))

      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.ACACIA_STAIRS).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.BIRCH_STAIRS).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.BRICK_STAIRS).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DARK_OAK_STAIRS).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.JUNGLE_STAIRS).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.OAK_STAIRS).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.SPRUCE_STAIRS).xp(2.0))

      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_STAIRS).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_BRICK_STAIRS).xp(2.0))

      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_PICKAXE).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_AXE).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_SHOVEL).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_HOE).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_SWORD).xp(2.0))

      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_PICKAXE).level(10).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_AXE).level(10).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_SHOVEL).level(10).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_HOE).level(10).xp(2.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_SWORD).level(10).xp(2.0))

      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.IRON_PICKAXE).level(20).xp(5.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.IRON_AXE).level(20).xp(5.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.IRON_SHOVEL).level(20).xp(5.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.IRON_HOE).level(20).xp(5.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.IRON_SWORD).level(20).xp(5.0))

      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.GOLDEN_PICKAXE).level(30).xp(10.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.GOLDEN_AXE).level(30).xp(10.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.GOLDEN_SHOVEL).level(30).xp(10.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.GOLDEN_HOE).level(30).xp(10.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.GOLDEN_SWORD).level(30).xp(10.0))

      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DIAMOND_PICKAXE).level(40).xp(20.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DIAMOND_AXE).level(40).xp(20.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DIAMOND_SHOVEL).level(40).xp(20.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DIAMOND_HOE).level(40).xp(20.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DIAMOND_SWORD).level(40).xp(20.0))

      // Mod Tools
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:pickaxe").level(70))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:hammer").level(70))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:mattock").level(70))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:scythe").level(70))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:kama").level(70))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:shovel").level(70))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:excavator").level(70))

      // Fallback
      //.addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().inverseQuery().xp(1.0))

      // Almura Seeds
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/agave_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/alfalfa_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/bambooshoot_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/barley_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/basil_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/bellpepper_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/blackberry_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/blackroot_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/blueberry_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/broccoli_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/butterbean_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/cabbage_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/celery_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/chilipepper_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/chive_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/cilantro_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/clove_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/coffee_bean_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/concord_grape_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/corn_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/cotton_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/cranberry_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/cucumber_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/cumin_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/daikon_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/dill_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/eggplant_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/fargreen_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/garlic_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/honeydew_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/hop_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/laurel_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/leek_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/lettuce_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/lotusroot_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/luffa_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/mint_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/oat_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/olive_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/onion_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/oregano_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/parsley_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/pea_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/peanut_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/peppercorn_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/pineapple_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/radish_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/raspberry_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/rice_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/rosemary_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/rye_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/sesame_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/sorghum_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/soybean_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/spinach_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/strawberry_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/sugarbeet_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/sweetpepper_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/sweetpotato_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/tarragon_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/thyme_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/tobacco_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/tomato_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/turnip_seed").xp(1.0))
      .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:seed/white_grape_seed").xp(1.0))
    ;

    // Interact?
    final BlockChain blockInteractChain = new BlockChain().matchTypeOnly().denyLevelRequired(CommonRegistar.createDenyAction("use"));

    listener

      // Mods
      .addBlockChain(InteractBlockEvent.Secondary.class, type, new BlockChain().from(blockInteractChain).queryDomain("ic2").level(60))
      .addBlockChain(InteractBlockEvent.Secondary.class, type, new BlockChain().from(blockInteractChain).queryDomain("buildcraft").level(60))
      .addBlockChain(InteractBlockEvent.Secondary.class, type, new BlockChain().from(blockInteractChain).queryDomain("railcraft").level(60))
    ;

    // Messages (Xp change/Level change
    listener
      .addMessageChain(Event.class, type, CommonRegistar.XP_TO_ACTION_BAR)
      .addMessageChain(Event.class, type, CommonRegistar.LEVEL_UP_TO_CHAT)
    ;

    // Effects (Xp change/Level change)
    listener
      .addEffectChain(Event.class, type, CommonRegistar.createFireworkEffect(SkillsImpl.ID + ":firework/crafting-level-up"))
    ;
  }
  // @formatter:on
}
