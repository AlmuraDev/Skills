/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE <https://github.com/InspireNXE/>
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
package org.inspirenxe.skills.impl.skill.builtin;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.level.LevelFunction;
import org.inspirenxe.skills.impl.Constants;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.level.UnknownLevelFunction;
import org.inspirenxe.skills.impl.skill.SkillTypeBuilderImpl;
import org.inspirenxe.skills.impl.skill.SkillTypeImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;

public class FarmingSkillType extends SkillTypeImpl {

    public FarmingSkillType() {
        super(Constants.Plugin.ID + ":farming", "Farming",
                (SkillTypeBuilderImpl) Sponge.getRegistry().createBuilder(SkillType.Builder.class)
                        .minLevel(1)
                        .maxLevel(99)
                        .levelFunction(Sponge.getRegistry().getType(LevelFunction.class, Constants.Plugin.ID + ":mmo_style").orElse(
                                UnknownLevelFunction.instance)));

        Sponge.getEventManager().registerListeners(SkillsImpl.instance.container, this);
    }

    @Listener(order = Order.LAST)
    public void onChangeBlockPlaceByPlayer(ChangeBlockEvent.Place event, @Root Player player) {
        if (!player.get(Keys.GAME_MODE).get().equals(GameModes.SURVIVAL)) {
            return;
        }

        final ItemStackSnapshot usedSnapshot = event.getContext().get(EventContextKeys.USED_ITEM).orElse(null);

        if (usedSnapshot == null) {
            return;
        }

        final SkillHolder skillHolder = SkillsImpl.instance.skillManager.getHolder(player.getWorld().getUniqueId(), player.getUniqueId()).orElse
                (null);

        if (skillHolder == null) {
            return;
        }

        final Skill skill = skillHolder.getSkill(this).orElse(null);

        if (skill == null) {
            return;
        }

        final int currentlevel = skill.getCurrentLevel();

        final int requiredLevel = this.canUseItem(usedSnapshot);

        if (currentlevel < requiredLevel) {
            player.sendMessage(Text.of("This item requires lvl " + requiredLevel + " to use!"));
            event.setCancelled(true);
            return;
        }

        final double experience = this.getPlaceExperience(usedSnapshot);

        if (experience != 0) {
            skill.addExperience(experience);
            skill.setDirtyState(true);
        }
    }

    @Listener(order = Order.LAST)
    public void onDropItemEventDestruct(DropItemEvent.Destruct event, @Root BlockSnapshot snapshot, @First Player player) {

        if (!player.get(Keys.GAME_MODE).get().equals(GameModes.SURVIVAL)) {
            return;
        }

        final SkillHolder skillHolder = SkillsImpl.instance.skillManager.getHolder(player.getWorld().getUniqueId(), player.getUniqueId()).orElse
                (null);

        if (skillHolder == null) {
            return;
        }

        final Skill skill = skillHolder.getSkill(this).orElse(null);

        if (skill == null) {
            return;
        }

        final User owner = event.getContext().get(EventContextKeys.OWNER).orElse(null);

        if (owner == null || !owner.getProfile().getUniqueId().equals(player.getUniqueId())) {
            return;
        }

        for (Entity entity : event.getEntities()) {
            if (!(entity instanceof Item)) {
                continue;
            }

            final Item item = (Item) entity;

            skill.addExperience(this.getHarvestExperience(item, skill.getCurrentLevel()));
        }
    }

    private int canUseItem(ItemStackSnapshot snapshot) {
        final ItemType itemType = snapshot.getType();
        final BlockType blockForItem = itemType.getBlock().orElse(null);

        if (itemType.equals(ItemTypes.CARROT)) {
            return 10;
        }

        if (itemType.equals(ItemTypes.POTATO)) {
            return 20;
        }

        if (itemType.equals(ItemTypes.MELON_SEEDS)) {
            return 30;
        }

        if (blockForItem != null) {
            if (blockForItem.equals(BlockTypes.REEDS)) {
                return 40;
            }
        }

        if (itemType.equals(ItemTypes.PUMPKIN_SEEDS)) {
            return 50;
        }

        if (itemType.equals(ItemTypes.BEETROOT_SEEDS)) {
            return 60;
        }

        if (itemType.equals(ItemTypes.NETHER_WART)) {
            return 70;
        }

        return 1;
    }

    private double getPlaceExperience(ItemStackSnapshot snapshot) {
        final ItemType itemType = snapshot.getType();
        final BlockType blockForItem = itemType.getBlock().orElse(null);

        if (itemType.equals(ItemTypes.WHEAT_SEEDS)) {
            return 1.5;
        }

        if (itemType.equals(ItemTypes.CARROT)) {
            return 3;
        }

        if (itemType.equals(ItemTypes.POTATO)) {
            return 4.5;
        }

        if (itemType.equals(ItemTypes.MELON_SEEDS)) {
            return 6;
        }

        if (blockForItem != null) {
            if (blockForItem.equals(BlockTypes.REEDS)) {
                return 7.5;
            }
        }

        if (itemType.equals(ItemTypes.PUMPKIN_SEEDS)) {
            return 9;
        }

        if (itemType.equals(ItemTypes.BEETROOT_SEEDS)) {
            return 10.5;
        }

        if (itemType.equals(ItemTypes.NETHER_WART)) {
            return 12;
        }

        return 0;
    }

    private double getHarvestExperience(Item item, int currentLevel) {
        final ItemStackSnapshot snapshot = item.item().get();
        final ItemType itemType = snapshot.getType();
        final BlockType blockForItem = itemType.getBlock().orElse(null);
        final int stackSize = snapshot.getQuantity();

        if (itemType.equals(ItemTypes.WHEAT_SEEDS)) {
            return stackSize * 5;
        }

        if (itemType.equals(ItemTypes.WHEAT)) {
            return stackSize * 20;
        }

        if (itemType.equals(ItemTypes.CARROT) && currentLevel >= 10) {
            return 12.5;
        }

        if (itemType.equals(ItemTypes.POTATO) && currentLevel >= 20) {
            return 15;
        }

        if (itemType.equals(ItemTypes.MELON) && currentLevel >= 30) {
            return 17.5;
        }

        if (blockForItem != null) {
            if (blockForItem.equals(BlockTypes.REEDS) && currentLevel >= 40) {
                return 30;
            }
        }

        if (itemType.equals(ItemTypes.PUMPKIN) && currentLevel >= 50) {
            return 75;
        }

        if (itemType.equals(ItemTypes.BEETROOT_SEEDS) && currentLevel >= 60) {
            return 15;
        }

        if (itemType.equals(ItemTypes.BEETROOT) && currentLevel >= 60) {
            return 100;
        }

        if (itemType.equals(ItemTypes.NETHER_WART) && currentLevel >= 70) {
            return 50;
        }

        return 0;
    }
}
