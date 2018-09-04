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
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.ItemChain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.feedback.MessageBuilder;
import org.inspirenxe.skills.impl.effect.SkillsEffectType;
import org.inspirenxe.skills.impl.effect.firework.SkillsFireworkEffectType;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class CraftingRegistar {

    private CraftingRegistar() {
    }

    @Inject
    private static GameRegistry registry;

    @Inject
    private static BuiltinEventListener listener;

    public static void configure() {
        final SkillType type = registry.getType(SkillType.class, "skills:crafting").orElse(null);

        if (type == null) {
            return;
        }

        // Craft Item
        final ItemChain craftChain = new ItemChain().matchTypeOnly().denyLevelRequired(
            (player, skill, value) -> player.sendMessage(Text.of("You require ", skill.getSkillType().getFormattedName(), " level ", value, " to "
                + "craft this."))
        );

        listener
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().inverseQuery().xp(1.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.WOODEN_PICKAXE).xp(5.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.STONE_PICKAXE).level(10).xp(10.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.IRON_PICKAXE).level(20).xp(20.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.GOLDEN_PICKAXE).level(30).xp(30.0))
            .addItemChain(CraftItemEvent.Craft.class, type, new ItemChain().from(craftChain).query(ItemTypes.DIAMOND_PICKAXE).level(40).xp(40.0));

        // Messages (Xp change/Level change
        listener
            .addMessageChain(Event.class, type, new MessageBuilder().chatType(ChatTypes.ACTION_BAR).xpGained((skill, xp) -> Text.of("+ ",
                SkillsConstants.XP_PRINTOUT.format(xp), "xp ", skill.getSkillType().getFormattedName()))
            )
            .addMessageChain(Event.class, type, new MessageBuilder().chatType(ChatTypes.CHAT).levelGained(
                (skill, integer) -> Text.of("Congratulations, you just advanced a new ", skill.getSkillType().getFormattedName(), " level! You are "
                    + "now level ", integer, "."))
            );

        // Effects (Xp change/Level change)
        listener
            .addEffectChain(Event.class, type, new EffectBuilder().levelGained(new BiFunction<Skill, Integer, List<SkillsEffectType>>() {
                    private final List<SkillsEffectType> level;
                    private final List<SkillsEffectType> levelMax;

                    {
                        final SkillsFireworkEffectType fireworkOnLevel = (SkillsFireworkEffectType) registry.getType(
                            FireworkEffectType.class, "skills:firework/crafting-level-up").orElse(null);

                        if (fireworkOnLevel == null) {
                            this.level = new ArrayList<>();
                            this.levelMax = new ArrayList<>();
                        } else {
                            this.level = Lists.newArrayList(fireworkOnLevel);
                            this.levelMax = Lists.newArrayList(fireworkOnLevel);

                            for (int i = 0; i < 3; i++) {
                                final ContentFireworkEffectTypeBuilderImpl builder = new ContentFireworkEffectTypeBuilderImpl();
                                this.levelMax.add(builder.from(fireworkOnLevel).build());
                            }
                        }
                    }

                    @Override
                    public List<SkillsEffectType> apply(final Skill skill, final Integer integer) {
                        if (integer == 99) {
                            return this.levelMax;
                        }

                        return this.level;
                    }
                })
            );
    }
}
