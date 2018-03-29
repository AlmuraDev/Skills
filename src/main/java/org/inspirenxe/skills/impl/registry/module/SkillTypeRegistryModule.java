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
package org.inspirenxe.skills.impl.registry.module;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.function.level.SkillsLevelFunction;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SkillTypeRegistryModule implements AdditionalCatalogRegistryModule<SkillType> {

  public static final SkillTypeRegistryModule instance = new SkillTypeRegistryModule();

  private final Map<String, SkillType> map = new HashMap<>();

  @Override
  public void registerAdditionalCatalog(SkillType catalogType) {
    checkNotNull(catalogType);
    this.map.put(catalogType.getId(), catalogType);
    catalogType.getLevelFunction().buildLevelTable(catalogType.getMaxLevel());
    ((SkillsLevelFunction) catalogType.getLevelFunction()).printTable();
  }

  @Override
  public Optional<SkillType> getById(String id) {
    return Optional.ofNullable(this.map.get(id));
  }

  @Override
  public Collection<SkillType> getAll() {
    return Collections.unmodifiableCollection(this.map.values());
  }
}
