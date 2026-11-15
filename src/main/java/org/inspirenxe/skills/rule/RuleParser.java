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

import org.apache.logging.log4j.Logger;
import org.inspirenxe.skills.config.ConfigLoader;
import org.inspirenxe.skills.config.SkillDefinition;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Color;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Turns the {@code groups} of a skill file into rules. A group sets defaults (trigger, creator, deny, level, xp, money) for its
 * {@code rules}; each rule can override them and lists the {@code blocks} or {@code items} it matches.
 *
 * <p>Unknown ids (e.g. of a mod that isn't installed) are logged and left out, the rest of the rule still applies.</p>
 */
public final class RuleParser {

    private final Logger logger;

    public RuleParser(final Logger logger) {
        this.logger = logger;
    }

    /**
     * @return The number of rules added
     */
    public int parse(final SkillDefinition definition, final RuleBook book) {
        final String file = definition.file().getFileName().toString();
        int added = 0;
        int unknown = 0;
        int groupIndex = 0;
        for (final ConfigurationNode group : definition.groups().childrenList()) {
            groupIndex++;
            int ruleIndex = 0;
            for (final ConfigurationNode node : group.node("rules").childrenList()) {
                ruleIndex++;
                final String where = file + " group " + groupIndex + " rule " + ruleIndex;
                final String triggerName = this.string(node, group, "trigger");
                if (triggerName == null) {
                    this.logger.error("Skipping {}: it has no trigger.", where);
                    continue;
                }
                final Trigger trigger = Trigger.fromConfig(triggerName).orElse(null);
                if (trigger == null) {
                    this.logger.error("Skipping {}: unknown trigger '{}'.", where, triggerName);
                    continue;
                }
                final String creatorName = this.string(node, group, "creator");
                Creator creator = Creator.ANY;
                if (creatorName != null) {
                    creator = Creator.fromConfig(creatorName).orElse(null);
                    if (creator == null) {
                        this.logger.error("Skipping {}: unknown creator '{}'.", where, creatorName);
                        continue;
                    }
                }
                final String key = trigger.matchesBlocks() ? "blocks" : "items";
                final String wrongKey = trigger.matchesBlocks() ? "items" : "blocks";
                if (!node.node(wrongKey).virtual()) {
                    this.logger.error("Skipping {}: trigger '{}' matches {}, not {}.", where, trigger.configName(), key, wrongKey);
                    continue;
                }
                final List<Matcher<?>> matchers = new ArrayList<>();
                for (final String entry : ConfigLoader.strings(node.node(key))) {
                    try {
                        matchers.add(Matcher.parse(entry, trigger.matchesBlocks()));
                    } catch (final IllegalArgumentException e) {
                        unknown++;
                        this.logger.debug("Leaving out '{}' of {}: {}", entry, where, e.getMessage());
                        if (!e.getMessage().startsWith("Unknown id")) {
                            this.logger.warn("Leaving out '{}' of {}: {}", entry, where, e.getMessage());
                        }
                    }
                }
                if (matchers.isEmpty()) {
                    continue;
                }
                final Integer level = this.integer(node, group, "level");
                final Double xp = this.decimal(node, group, "xp");
                final Double money = this.decimal(node, group, "money");
                final String deny = this.string(node, group, "deny");
                book.add(definition.type(), new Rule(trigger, List.copyOf(matchers), level, xp, money, creator, deny));
                added++;
            }
        }
        if (unknown > 0) {
            this.logger.info("{}: left out {} unknown block/item ids (mods that aren't installed?). Turn on debug logging to list them.",
                    file, unknown);
        }
        return added;
    }

    /**
     * @return The firework launched when a player of this skill levels up, if the skill has one
     */
    public Optional<FireworkEffect> firework(final SkillDefinition definition) {
        final ConfigurationNode node = definition.firework();
        if (node.virtual() || node.empty()) {
            return Optional.empty();
        }
        final String file = definition.file().getFileName().toString();
        try {
            final String shapeName = node.node("shape").getString("star");
            // Shapes are sponge:burst, sponge:creeper, sponge:large_ball, sponge:small_ball and sponge:star
            final ResourceKey shapeKey = shapeName.indexOf(':') < 0 ? ResourceKey.sponge(shapeName) : ResourceKey.resolve(shapeName);
            final FireworkShape shape = RegistryTypes.FIREWORK_SHAPE.get().<FireworkShape>findValue(shapeKey)
                    .orElseThrow(() -> new IllegalArgumentException("unknown shape '" + shapeName + "'"));
            final List<Color> colors = RuleParser.colors(node.node("colors"));
            if (colors.isEmpty()) {
                throw new IllegalArgumentException("it needs at least one color");
            }
            return Optional.of(FireworkEffect.builder()
                    .shape(shape)
                    .colors(colors)
                    .fades(RuleParser.colors(node.node("fade-colors")))
                    .flicker(node.node("flicker").getBoolean(false))
                    .trail(node.node("trail").getBoolean(false))
                    .build());
        } catch (final RuntimeException e) {
            this.logger.error("Ignoring the level-up-firework of {}: {}", file, e.getMessage());
            return Optional.empty();
        }
    }

    private static List<Color> colors(final ConfigurationNode node) {
        final List<Color> colors = new ArrayList<>();
        for (final String value : ConfigLoader.strings(node)) {
            final String hex = value.trim().startsWith("#") ? value.trim().substring(1) : value.trim();
            try {
                colors.add(Color.ofRgb(Integer.parseInt(hex, 16)));
            } catch (final NumberFormatException e) {
                throw new IllegalArgumentException("'" + value + "' is no #RRGGBB color");
            }
        }
        return colors;
    }

    private String string(final ConfigurationNode rule, final ConfigurationNode group, final String key) {
        final String value = rule.node(key).getString();
        return value != null ? value : group.node(key).getString();
    }

    private Integer integer(final ConfigurationNode rule, final ConfigurationNode group, final String key) {
        final ConfigurationNode node = !rule.node(key).virtual() ? rule.node(key) : group.node(key);
        return node.virtual() || node.raw() == null ? null : node.getInt();
    }

    private Double decimal(final ConfigurationNode rule, final ConfigurationNode group, final String key) {
        final ConfigurationNode node = !rule.node(key).virtual() ? rule.node(key) : group.node(key);
        return node.virtual() || node.raw() == null ? null : node.getDouble();
    }
}
