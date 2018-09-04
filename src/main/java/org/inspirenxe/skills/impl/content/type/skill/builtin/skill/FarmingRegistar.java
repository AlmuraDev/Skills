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
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.BlockChainBuilder;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.ItemChainBuilder;
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
import org.spongepowered.api.item.inventory.ItemStack;
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
        final SkillType skillType = registry.getType(SkillType.class, "skills:farming").orElse(null);

        if (skillType == null) {
            return;
        }

        final ItemChainBuilder interactChain = new ItemChainBuilder().matchTypeOnly().denyLevelRequired(
            (player, skill, value) -> player.sendMessage(Text.of("You require ", TextColors.DARK_GREEN, skill.getSkillType().getName(),
                TextColors.RESET, " level ", value, " to use this."))
        );

        // Interact Item
        listener
            .addItemChain(InteractItemEvent.class, skillType,
                new ItemChainBuilder().from(interactChain).query(ItemStack.of(ItemTypes.STONE_HOE, 1)).minLevel(10)
            )
            .addItemChain(InteractItemEvent.class, skillType,
                new ItemChainBuilder().from(interactChain).query(ItemStack.of(ItemTypes.IRON_HOE, 1)).minLevel(20)
            )
            .addItemChain(InteractItemEvent.class, skillType,
                new ItemChainBuilder().from(interactChain).query(ItemStack.of(ItemTypes.GOLDEN_HOE, 1)).minLevel(30)
            )
            .addItemChain(InteractItemEvent.class, skillType,
                new ItemChainBuilder().from(interactChain).query(ItemStack.of(ItemTypes.DIAMOND_HOE, 1)).minLevel(40)
            );

        listener
            .addItemChain(DropItemEvent.Destruct.class, skillType,
                new ItemChainBuilder().from(interactChain).query(ItemStack.of(ItemTypes.WHEAT_SEEDS, 1)).xp(20.0).economy(1.0)
            )
            .addItemChain(DropItemEvent.Destruct.class, skillType,
                new ItemChainBuilder().from(interactChain).query(ItemStack.of(ItemTypes.WHEAT, 1)).xp(50.0).economy(1.5)
            );

        // Place Block
        final BlockChainBuilder placeChain = new BlockChainBuilder().matchTypeOnly().denyLevelRequired(
            (player, skill, value) -> player.sendMessage(Text.of("You require ", TextColors.DARK_GREEN, skill.getSkillType().getName(),
                TextColors.RESET, " level ", value, " to plant this."))
        );

        listener
            .addBlockChain(ChangeBlockEvent.Place.class, skillType,
                new BlockChainBuilder().from(placeChain).query(BlockTypes.WHEAT.getDefaultState()).xp(10.0).economy(1.0)
            )
            .addBlockChain(ChangeBlockEvent.Place.class, skillType,
                new BlockChainBuilder().from(placeChain).query(BlockTypes.CARROTS.getDefaultState()).minLevel(10)
            );

        // Messages (Xp change/Level change
        listener
            .addMessageChain(Event.class, skillType, new MessageBuilder().chatType(ChatTypes.ACTION_BAR).xpGained((skill, xp) -> Text.of("+ ",
                SkillsConstants.XP_PRINTOUT.format(xp), "xp ", TextColors.DARK_GREEN, skill.getSkillType().getName()))
            )
            .addMessageChain(Event.class, skillType, new MessageBuilder().chatType(ChatTypes.CHAT).levelGained(
                (skill, integer) -> Text.of("Congratulations, you just advanced a new ", TextColors.DARK_GREEN, skill.getSkillType().getName(),
                    TextColors.RESET, " level! You are now level ", integer, "."))
            );

        // Effects (Xp change/Level change)
        listener
            .addEffectChain(Event.class, skillType, new EffectBuilder().levelGained(new BiFunction<Skill, Integer, List<SkillsEffectType>>() {
                    private final List<SkillsEffectType> normalLevelProg;
                    private final List<SkillsEffectType> level99Prog;

                    {
                        final SkillsFireworkEffectType fireworkOnLevel = (SkillsFireworkEffectType) registry.getType(
                            FireworkEffectType.class, "skills:firework/farming-level-up").orElse(null);

                        if (fireworkOnLevel == null) {
                            this.normalLevelProg = new ArrayList<>();
                            this.level99Prog = new ArrayList<>();
                        } else {
                            this.normalLevelProg = Lists.newArrayList(fireworkOnLevel);
                            this.level99Prog = Lists.newArrayList(fireworkOnLevel);

                            for (int i = 0; i < 3; i++) {
                                final ContentFireworkEffectTypeBuilderImpl builder = new ContentFireworkEffectTypeBuilderImpl();
                                this.level99Prog.add(builder.from(fireworkOnLevel).build());
                            }
                        }
                    }

                    @Override
                    public List<SkillsEffectType> apply(final Skill skill, final Integer integer) {
                        if (integer == 99) {
                            return this.level99Prog;
                        }

                        return this.normalLevelProg;
                    }
                })
            );
    }
}
