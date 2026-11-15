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
package org.inspirenxe.skills.rule;

import org.inspirenxe.skills.api.SkillType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Every rule of every skill, by trigger. Within one skill and trigger the first rule (in file order) that matches wins. The candidate
 * rules per block/item type are cached, so a lookup doesn't test every rule on each event. Only used on the server thread.
 */
public final class RuleBook {

    private final Map<Trigger, Map<SkillType, Index>> rules = new EnumMap<>(Trigger.class);

    public void add(final SkillType skill, final Rule rule) {
        this.rules.computeIfAbsent(rule.trigger(), t -> new HashMap<>()).computeIfAbsent(skill, s -> new Index()).rules.add(rule);
    }

    public boolean has(final Trigger trigger) {
        final Map<SkillType, Index> skills = this.rules.get(trigger);
        return skills != null && !skills.isEmpty();
    }

    public Optional<Rule> find(final Trigger trigger, final SkillType skill, final BlockState state) {
        final Index index = this.index(trigger, skill);
        if (index == null) {
            return Optional.empty();
        }
        for (final Rule rule : index.blockCandidates(state.type())) {
            if (rule.matches(state)) {
                return Optional.of(rule);
            }
        }
        return Optional.empty();
    }

    public Optional<Rule> find(final Trigger trigger, final SkillType skill, final ItemType type) {
        final Index index = this.index(trigger, skill);
        if (index == null) {
            return Optional.empty();
        }
        for (final Rule rule : index.itemCandidates(type)) {
            if (rule.matches(type)) {
                return Optional.of(rule);
            }
        }
        return Optional.empty();
    }

    /**
     * @return True if any skill has a rule of the trigger matching the item
     */
    public boolean matchesAny(final Trigger trigger, final ItemType type) {
        final Map<SkillType, Index> skills = this.rules.get(trigger);
        if (skills == null) {
            return false;
        }
        for (final Index index : skills.values()) {
            for (final Rule rule : index.itemCandidates(type)) {
                if (rule.matches(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    public int size() {
        int size = 0;
        for (final Map<SkillType, Index> skills : this.rules.values()) {
            for (final Index index : skills.values()) {
                size += index.rules.size();
            }
        }
        return size;
    }

    private Index index(final Trigger trigger, final SkillType skill) {
        final Map<SkillType, Index> skills = this.rules.get(trigger);
        return skills == null ? null : skills.get(skill);
    }

    private static final class Index {

        final List<Rule> rules = new ArrayList<>();
        final Map<BlockType, List<Rule>> blocks = new HashMap<>();
        final Map<ItemType, List<Rule>> items = new HashMap<>();

        List<Rule> blockCandidates(final BlockType type) {
            return this.blocks.computeIfAbsent(type, t -> {
                final List<Rule> list = new ArrayList<>();
                for (final Rule rule : this.rules) {
                    if (rule.couldMatchBlock(t)) {
                        list.add(rule);
                    }
                }
                return list.isEmpty() ? Collections.emptyList() : list;
            });
        }

        List<Rule> itemCandidates(final ItemType type) {
            return this.items.computeIfAbsent(type, t -> {
                final List<Rule> list = new ArrayList<>();
                for (final Rule rule : this.rules) {
                    if (rule.couldMatchItem(t)) {
                        list.add(rule);
                    }
                }
                return list.isEmpty() ? Collections.emptyList() : list;
            });
        }
    }
}
