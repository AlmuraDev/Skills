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

    public static void configure() {
        final SkillType type = registry.getType(SkillType.class, SkillsImpl.ID + ":crafting").orElse(null);

        if (type == null) {
            return;
        }

        // Craft
        final ItemChain craftChain = new ItemChain().matchTypeOnly().denyLevelRequired(CommonRegistar.createDenyAction("craft"));

        listener
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.PLANKS).xp(2.5))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_SLAB).xp(2.5))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.BED).xp(15.0))
            //.addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONEBRICK).xp(2.5))
            // .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_SLAB).xp(2.0))
            // .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_SLAB2).xp(2.0))

            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.ACACIA_STAIRS).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.BIRCH_STAIRS).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.BRICK_STAIRS).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DARK_OAK_STAIRS).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.JUNGLE_STAIRS).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.OAK_STAIRS).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.SPRUCE_STAIRS).xp(4.0))

            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_STAIRS).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_BRICK_STAIRS).xp(4.0))

            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_PICKAXE).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_AXE).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_SHOVEL).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_HOE).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_SWORD).xp(4.0))

            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_PICKAXE).level(10).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_AXE).level(10).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_SHOVEL).level(10).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_HOE).level(10).xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_SWORD).xp(4.0))

            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.IRON_PICKAXE).level(20).xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.IRON_AXE).level(20).xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.IRON_SHOVEL).level(20).xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.IRON_HOE).level(20).xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.IRON_SWORD).xp(8.0))

            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.GOLDEN_PICKAXE).level(30).xp(15.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.GOLDEN_AXE).level(30).xp(15.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.GOLDEN_SHOVEL).level(30).xp(15.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.GOLDEN_HOE).level(30).xp(15.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.GOLDEN_SWORD).level(30).xp(15.0))

            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DIAMOND_PICKAXE).level(40).xp(30.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DIAMOND_AXE).level(40).xp(30.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DIAMOND_SHOVEL).level(40).xp(30.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DIAMOND_HOE).level(40).xp(30.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DIAMOND_SWORD).level(40).xp(30.0))

            // Mod Tools
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:pickaxe").level(55))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:hammer").level(55))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:mattock").level(55))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:scythe").level(55))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:kama").level(55))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:shovel").level(55))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("tconstruct:excavator").level(55))

            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("sgcraft:sgcorecrystal").level(55).xp(30.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("sgcraft:sgcontrollercrystal").level(55).xp(30.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("sgcraft:stargatebase").level(60).xp(40.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("sgcraft:stargatering").level(60).xp(40.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("sgcraft:stargatecontroller").level(60).xp(50.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("sgcraft:zpm_interface_cart").level(70).xp(100.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("sgcraft:ic2powerunit").level(63).xp(30.0))

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

            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/bacon").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/ballofrice").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/bread_baguette").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/bread_butterandcheeseroll").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/bread_cinnamonroll").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/bread_croissant").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/bread_honeynutbread").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/bread_peasantsroll").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/bread_pretzel").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/bread_rosemarysagebread").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/bread_sesameseedbun").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/bread_walnutraisinbread").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/breakfast_omlette").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/breakfast_pancake").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/breakfast_scrambledeggs").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/breakfast_sunnysideegg").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/breakfast_waffle").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/cheese").xp(2.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/chickenleg").xp(2.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/coconutmilk").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/cooked_ground_beef").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/cooked_ground_chicken").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/farlands_potstickers").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/farlands_ramen_beef").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/farlands_ramen_chicken").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/farlands_ramen_seafood").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/farlands_riceball_fish").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/farlands_soup_eggdrop").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/farlands_stirfry_base").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/farlands_stirfry_eggplantlotus").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/farlands_stirfry_mushroom").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/farlands_stirfry_peppersteak").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/farlands_stirfry_vegetable").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/hamslice").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/honeypot").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/kebab_beef").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/kebab_chicken").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/kebab_leek").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/kebab_lizard").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/kebab_mushroom").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/kebab_seafood").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/lambchop").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/loafofbread").xp(1.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/mac_cheese").xp(4.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/meat_barbecue").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/meat_friedchicken").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/meaty_dog_bone").xp(1.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pasta_bolognese").xp(6.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pasta_carbonara").xp(6.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pasta_marinara").xp(6.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pasta_meatballs").xp(6.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pasta_meatdumpling").xp(6.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pasta_peperoncino").xp(6.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pasta_pescatore").xp(6.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pasta_ravioli").xp(6.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pepperoni").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pizza_carnivore").xp(15.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pizza_cheese").xp(10.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pizza_deluxe").xp(18.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pizza_islander").xp(13.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pizza_margherita").xp(12.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pizza_mushroom").xp(12.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pizza_pepperoni").xp(13.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pizza_sausage").xp(13.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/pizza_vegetable").xp(13.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/raisin").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/roastbeef").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_bbq").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_blt").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_cheeseburger").xp(14.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_cheeseburger1").xp(15.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_cheeseburger2").xp(16.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_chickenburger").xp(12.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_chickenburger1").xp(15.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_chickenburger2").xp(16.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_chilicheeseburger").xp(15.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_hamburger").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_hamburger1").xp(10.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_hamburger2").xp(11.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_veggieburger").xp(11.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_veggieburger1").xp(12.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_burger_veggieburger2").xp(13.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_cheese").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_cheese_hotdog").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_cheesesteak").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_chilidog").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_fish").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_grilledcheese").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_hamcheese").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_hotdog").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_pbj_apple").xp(10.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_pbj_grape").xp(10.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_pbj_orange").xp(10.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_pbj_strawberry").xp(10.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sandwich_roastbeef").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sliceofbread").xp(1.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/sausage").xp(2.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/snack_coleslaw").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/snack_crackers").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/snack_dillpickle").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/snack_mashedpotatos").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/snack_mixednuts").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/snack_piginblanket").xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/soup_clamchowder").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/soup_corn").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/soup_creamofmushroom").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/soup_minestrone").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/soup_onion").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/soup_pea").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/soup_pumpkin").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/soup_riceporridge").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/soup_tomato").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/soup_vegetable").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/southern_cornchips").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/southern_chili").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/southern_taco_beef").xp(9.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/southern_taco_chicken").xp(9.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/southern_taco_fish").xp(10.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/southern_taco_pork").xp(9.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/southern_wrap_beef").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/southern_wrap_chicken").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/southern_wrap_fish").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/southern_wrap_pork").xp(8.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/tofu").xp(1.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/food/wolfmeat").xp(1.0))

            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_apple").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_banana").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_blueberry").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_blackberry").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_cantaloupe").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_carrot").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_cherry").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_concord_grape").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_cranberry").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_dragonfruit").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_grapefruit").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_honeydew").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_kiwi").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_lemon").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_lime").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_mixedgrape").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_mixedvegetable").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_orange").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_peach").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_pineapple").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_plum").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_raspberry").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_strawberry").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_tomato").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_watermelon").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_whitegrape").xp(7.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("almura:food/drink/juice_yummyberry").xp(7.0))

            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("minecraft:furnace").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("minecraft:crafting_table").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("minecraft:sign").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("minecraft:wooden_door").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("minecraft:iron_door").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("minecraft:spruce_door").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("minecraft:jungle_door").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("minecraft:dark_oak_door").xp(3.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query("minecraft:acacia_door").xp(3.0))
        ;

        // Interact?
        final BlockChain blockInteractChain = new BlockChain().matchTypeOnly().denyLevelRequired(CommonRegistar.createDenyAction("use"));

        listener

            // Mods
            .addBlockChain(InteractBlockEvent.Secondary.class, type, new BlockChain().from(blockInteractChain).queryDomain("ic2").level(35))
            .addBlockChain(InteractBlockEvent.Secondary.class, type, new BlockChain().from(blockInteractChain).queryDomain("buildcraft").level(40))
            .addBlockChain(InteractBlockEvent.Secondary.class, type, new BlockChain().from(blockInteractChain).queryDomain("railcraft").level(45))
            .addBlockChain(InteractBlockEvent.Secondary.class, type, new BlockChain().from(blockInteractChain).queryDomain("tconstruct").level(55))
        ;

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
