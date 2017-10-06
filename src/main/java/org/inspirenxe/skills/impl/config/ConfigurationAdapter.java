/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package org.inspirenxe.skills.impl.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigurationAdapter<T extends Configuration> {

    private final Class<T> type;
    private final Path configPath;
    private final ConfigurationLoader<? extends ConfigurationNode> configLoader;
    private final ObjectMapper<T>.BoundInstance mapper;
    private ConfigurationNode root;
    private T config;

    public ConfigurationAdapter(final Class<T> type, final Path configPath, final ConfigurationLoader<? extends ConfigurationNode> configLoader) {
        this.type = type;
        this.configPath = configPath;
        this.configLoader = configLoader;
        try {
            this.mapper = ObjectMapper.forClass(type).bindToNew();
        } catch (final ObjectMappingException e) {
            throw new RuntimeException("Failed to construct mapper for config class [" + type + "]!", e);
        }
        this.init();
    }

    private void init() {
        if (Files.notExists(this.configPath)) {
            try {
                if (Files.notExists(this.configPath.getParent())) {
                    Files.createDirectories(this.configPath.getParent());
                }

                this.root = SimpleConfigurationNode.root(this.configLoader.getDefaultOptions());
                this.save();
            } catch (IOException | ObjectMappingException e) {
                e.printStackTrace();
            }
        }

        try {
            this.load();
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    public T getConfig() {
        return this.config;
    }

    public void load() throws IOException, ObjectMappingException {
        this.root = this.configLoader.load();
        this.config = this.mapper.populate(this.root);
    }

    public void save() throws IOException, ObjectMappingException {
        this.mapper.serialize(this.root);
        this.configLoader.save(this.root);
    }
}
