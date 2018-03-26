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
