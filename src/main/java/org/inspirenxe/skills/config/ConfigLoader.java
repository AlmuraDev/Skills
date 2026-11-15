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
package org.inspirenxe.skills.config;

import org.apache.logging.log4j.Logger;
import org.inspirenxe.skills.api.Skills;
import org.inspirenxe.skills.core.SkillTypeImpl;
import org.inspirenxe.skills.function.FormulaEconomyFunction;
import org.inspirenxe.skills.function.FormulaLevelFunction;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Reads {@code config/skills/skills.conf} and the skill files in {@code config/skills/skills}, writing the bundled defaults first if
 * they are missing.
 */
public final class ConfigLoader {

    public static final String MAIN_FILE = "skills.conf";
    public static final String SKILLS_DIR = "skills";
    private static final String[] DEFAULT_SKILLS = {"crafting", "digger", "farming", "mining", "woodcutting"};
    private static final Pattern SKILL_FILE_NAME = Pattern.compile("[a-z0-9_.-]+");

    private final Logger logger;
    private final Path configDir;

    public ConfigLoader(final Logger logger, final Path configDir) {
        this.logger = logger;
        this.configDir = configDir;
    }

    /**
     * Writes the bundled default files that don't exist yet. The skill files are only written when the skills folder is created, so
     * a skill deleted on purpose stays deleted.
     */
    public void writeDefaults() throws IOException {
        Files.createDirectories(this.configDir);
        this.copyDefault(ConfigLoader.MAIN_FILE, this.configDir.resolve(ConfigLoader.MAIN_FILE));
        final Path skillsDir = this.configDir.resolve(ConfigLoader.SKILLS_DIR);
        if (Files.notExists(skillsDir)) {
            Files.createDirectories(skillsDir);
            for (final String skill : ConfigLoader.DEFAULT_SKILLS) {
                this.copyDefault(ConfigLoader.SKILLS_DIR + "/" + skill + ".conf", skillsDir.resolve(skill + ".conf"));
            }
        }
    }

    private void copyDefault(final String asset, final Path target) throws IOException {
        if (Files.exists(target)) {
            return;
        }
        try (InputStream in = ConfigLoader.class.getResourceAsStream("/assets/skills/" + asset)) {
            if (in == null) {
                throw new IOException("The default '" + asset + "' is missing from the Skills jar");
            }
            Files.copy(in, target);
        }
        this.logger.info("Wrote the default '{}'.", target);
    }

    public SkillsConfig loadMain() throws ConfigurateException {
        final CommentedConfigurationNode root = this.load(this.configDir.resolve(ConfigLoader.MAIN_FILE));
        final String path = root.node("database", "file").getString("./config/skills/skills-h2");
        final Map<ResourceKey, ResourceKey> shares = new LinkedHashMap<>();
        for (final Map.Entry<Object, CommentedConfigurationNode> entry : root.node("container-share").childrenMap().entrySet()) {
            final String from = entry.getValue().getString();
            if (from == null || from.isBlank()) {
                continue;
            }
            try {
                shares.put(ResourceKey.resolve(String.valueOf(entry.getKey())), ResourceKey.resolve(from));
            } catch (final RuntimeException e) {
                this.logger.warn("Ignoring the container share '{}' = '{}' in {}: {}", entry.getKey(), from, ConfigLoader.MAIN_FILE,
                        e.getMessage());
            }
        }
        final int saveInterval = Math.max(5, root.node("save-interval").getInt(60));
        return new SkillsConfig(Path.of(path), shares, saveInterval);
    }

    /**
     * Reads every {@code .conf} file in the skills folder. A file with an error is skipped (and logged), the others still load.
     */
    public List<SkillDefinition> loadSkills() throws IOException {
        final List<SkillDefinition> skills = new ArrayList<>();
        final Path dir = this.configDir.resolve(ConfigLoader.SKILLS_DIR);
        if (Files.notExists(dir)) {
            return skills;
        }
        final List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.conf")) {
            stream.forEach(files::add);
        }
        files.sort(null);
        for (final Path file : files) {
            final String fileName = file.getFileName().toString();
            final String id = fileName.substring(0, fileName.length() - ".conf".length()).toLowerCase(Locale.ROOT);
            if (!ConfigLoader.SKILL_FILE_NAME.matcher(id).matches()) {
                this.logger.error("Skipping the skill file '{}': the name may only use a-z, 0-9, '_', '-' and '.'.", file);
                continue;
            }
            try {
                skills.add(this.loadSkill(file, ResourceKey.of(Skills.ID, id)));
            } catch (final Exception e) {
                this.logger.error("Skipping the skill file '{}': {}", file, e.getMessage());
            }
        }
        return skills;
    }

    private SkillDefinition loadSkill(final Path file, final ResourceKey key) throws ConfigurateException {
        final CommentedConfigurationNode root = this.load(file);
        final String name = root.node("name").getString(key.value());
        final int maxLevel = root.node("max-level").getInt(99);
        final String levelFormula = root.node("level-formula").getString();
        if (levelFormula == null || levelFormula.isBlank()) {
            throw new IllegalArgumentException("'level-formula' is missing");
        }
        final FormulaLevelFunction levelFunction;
        try {
            levelFunction = new FormulaLevelFunction(levelFormula, maxLevel);
        } catch (final RuntimeException e) {
            throw new IllegalArgumentException("'level-formula' is invalid: " + e.getMessage(), e);
        }
        final String economyFormula = root.node("economy-formula").getString();
        FormulaEconomyFunction economyFunction = null;
        if (economyFormula != null && !economyFormula.isBlank()) {
            try {
                economyFunction = new FormulaEconomyFunction(economyFormula);
            } catch (final RuntimeException e) {
                throw new IllegalArgumentException("'economy-formula' is invalid: " + e.getMessage(), e);
            }
        }
        final SkillTypeImpl type = new SkillTypeImpl(key, name, levelFunction, economyFunction, maxLevel);
        return new SkillDefinition(file, type, root.node("level-up-firework"), root.node("groups"));
    }

    /**
     * Reads only the rules and firework of the skill files again, for a reload. Skills that were added or removed since the server
     * started are reported and left out until the next restart.
     */
    public List<SkillDefinition> reloadRules(final List<SkillDefinition> current) {
        final List<SkillDefinition> reloaded = new ArrayList<>();
        for (final SkillDefinition definition : current) {
            try {
                final CommentedConfigurationNode root = this.load(definition.file());
                reloaded.add(new SkillDefinition(definition.file(), definition.type(), root.node("level-up-firework"), root.node("groups")));
            } catch (final Exception e) {
                this.logger.error("Keeping the old rules of '{}', it failed to load: {}", definition.file(), e.getMessage());
                reloaded.add(definition);
            }
        }
        return reloaded;
    }

    private CommentedConfigurationNode load(final Path file) throws ConfigurateException {
        return HoconConfigurationLoader.builder().path(file).build().load();
    }

    /**
     * @return The child of the node as a list of strings: a list, or one string
     */
    public static List<String> strings(final ConfigurationNode node) {
        final List<String> values = new ArrayList<>();
        if (node.isList()) {
            for (final ConfigurationNode child : node.childrenList()) {
                final String value = child.getString();
                if (value != null) {
                    values.add(value);
                }
            }
        } else if (!node.virtual() && node.getString() != null) {
            values.add(node.getString());
        }
        return values;
    }
}
