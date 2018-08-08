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
package org.inspirenxe.skills.impl.content.registry.module;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Singleton;
import org.inspirenxe.skills.api.color.ColorType;
import org.inspirenxe.skills.impl.color.ColorTypeImpl;
import org.inspirenxe.skills.impl.content.registry.CatalogKey;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.DelayedRegistration;
import org.spongepowered.api.util.Color;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public final class ColorTypeRegistryModule implements AdditionalCatalogRegistryModule<ColorType> {

  public static final ColorTypeRegistryModule instance = new ColorTypeRegistryModule();

  private final Map<String, ColorType> map = new HashMap<>();

  @DelayedRegistration(RegistrationPhase.PRE_INIT)
  @Override
  public void registerDefaults() {
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:black"), Color.BLACK));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:blue"), Color.BLUE));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:cyan"), Color.CYAN));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:dark_cyan"), Color.DARK_CYAN));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:dark_green"), Color.DARK_GREEN));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:dark_magenta"), Color.DARK_MAGENTA));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:gray"), Color.GRAY));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:green"), Color.GREEN));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:lime"), Color.LIME));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:magenta"), Color.MAGENTA));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:navy"), Color.NAVY));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:pink"), Color.PINK));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:purple"), Color.PURPLE));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:red"), Color.RED));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:white"), Color.WHITE));
    this.registerAdditionalCatalog(new ColorTypeImpl(new CatalogKey("minecraft:yellow"), Color.YELLOW));
  }

  @Override
  public void registerAdditionalCatalog(final ColorType extraCatalog) {
    checkNotNull(extraCatalog);
    this.map.put(extraCatalog.getId(), extraCatalog);
  }

  @Override
  public Optional<ColorType> getById(final String id) {
    checkNotNull(id);
    return Optional.ofNullable(this.map.get(id));
  }

  @Override
  public Collection<ColorType> getAll() {
    return Collections.unmodifiableCollection(this.map.values());
  }
}
