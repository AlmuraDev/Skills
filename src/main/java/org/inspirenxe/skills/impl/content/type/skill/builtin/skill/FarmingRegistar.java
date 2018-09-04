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

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.effect.firework.FireworkEffectType;
import org.inspirenxe.skills.impl.SkillsConstants;
import org.inspirenxe.skills.impl.content.type.effect.firework.ContentFireworkEffectTypeBuilderImpl;
import org.inspirenxe.skills.impl.content.type.skill.builtin.BuiltinEventListener;
import org.inspirenxe.skills.impl.content.type.skill.builtin.EffectBuilder;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.BlockChain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.ItemChain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.feedback.MessageBuilder;
import org.inspirenxe.skills.impl.effect.SkillsEffectType;
import org.inspirenxe.skills.impl.effect.firework.SkillsFireworkEffectType;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class FarmingRegistar {

    private FarmingRegistar() {
    }

    @Inject
    private static GameRegistry registry;

    @Inject
    private static BuiltinEventListener listener;

    public static void configure() {
        final SkillType type = registry.getType(SkillType.class, "skills:farming").orElse(null);

        if (type == null) {
            return;
        }

        // Interact Item
        final ItemChain interactChain = new ItemChain().matchTypeOnly().denyLevelRequired(
            (player, skill, value) -> player.sendMessage(Text.of("You require ", TextColors.DARK_GREEN, skill.getSkillType().getName(),
                TextColors.RESET, " level ", value, " to use this."))
        );

        listener
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.STONE_HOE).level(10))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.IRON_HOE).level(20))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.GOLDEN_HOE).level(30))
            .addItemChain(InteractItemEvent.class, type, new ItemChain().from(interactChain).query(ItemTypes.DIAMOND_HOE).level(40));

        // Place Crops
        final BlockChain placeChain = new BlockChain().matchTypeOnly().denyLevelRequired(
            (player, skill, value) -> player.sendMessage(Text.of("You require ", TextColors.DARK_GREEN, skill.getSkillType().getName(),
                TextColors.RESET, " level ", value, " to plant this."))
        );

        listener
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query(BlockTypes.WHEAT).xp(1.0).economy(1.0))
            .addBlockChain(ChangeBlockEvent.Place.class, type, new BlockChain().from(placeChain).query(BlockTypes.CARROTS).level(10).xp(2.0).economy(1.0));

        // Break crops
        final BlockChain breakChain = new BlockChain().matchTypeOnly().denyLevelRequired(
            (player, skill, value) -> player.sendMessage(Text.of("You require ", TextColors.DARK_GREEN, skill.getSkillType().getName(),
                TextColors.RESET, " level ", value, " to break this."))
        );

        listener
            .addBlockChain(ChangeBlockEvent.Break.class, type, new BlockChain().from(breakChain).query(BlockTypes.CARROTS).level(10));

        // Harvest crops
        final ItemChain dropsChain = new ItemChain().matchTypeOnly();

        listener
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query(ItemTypes.WHEAT_SEEDS).xp(1.0).economy(1.0))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query(ItemTypes.WHEAT).xp(10.0).economy(1.5))
            .addItemChain(DropItemEvent.Destruct.class, type, new ItemChain().from(dropsChain).query(ItemTypes.CARROT).xp(20.0).economy(1.5));

        // Messages (Xp change/Level change
        listener
            .addMessageChain(Event.class, type, new MessageBuilder().chatType(ChatTypes.ACTION_BAR).xpGained(
                (skill, xp) -> Text.of("+ ", SkillsConstants.XP_PRINTOUT.format(xp), "xp ", TextColors.DARK_GREEN, skill.getSkillType().getName())))
            .addMessageChain(Event.class, type, new MessageBuilder().chatType(ChatTypes.CHAT).levelGained(
                (skill, integer) -> Text.of("Congratulations, you just advanced a new ", TextColors.DARK_GREEN, skill.getSkillType().getName(),
                    TextColors.RESET, " level! You are now level ", integer, ".")));

        // Effects (Xp change/Level change)
        listener
            .addEffectChain(Event.class, type, new EffectBuilder().levelGained(
                new BiFunction<Skill, Integer, List<SkillsEffectType>>() {
                    private final List<SkillsEffectType> level;
                    private final List<SkillsEffectType> maxLevel;

                    {
                        final SkillsFireworkEffectType fireworkOnLevel = (SkillsFireworkEffectType) registry.getType(
                            FireworkEffectType.class, "skills:firework/farming-level-up").orElse(null);

                        if (fireworkOnLevel == null) {
                            this.level = new ArrayList<>();
                            this.maxLevel = new ArrayList<>();
                        } else {
                            this.level = Lists.newArrayList(fireworkOnLevel);
                            this.maxLevel = Lists.newArrayList(fireworkOnLevel);

                            for (int i = 0; i < 3; i++) {
                                final ContentFireworkEffectTypeBuilderImpl builder = new ContentFireworkEffectTypeBuilderImpl();
                                this.maxLevel.add(builder.from(fireworkOnLevel).build());
                            }
                        }
                    }

                    @Override
                    public List<SkillsEffectType> apply(final Skill skill, final Integer integer) {
                        if (integer == 99) {
                            return this.maxLevel;
                        }

                        return this.level;
                    }
                })
            );
    }
}
