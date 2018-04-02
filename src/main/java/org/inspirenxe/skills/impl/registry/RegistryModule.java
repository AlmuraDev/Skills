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
package org.inspirenxe.skills.impl.registry;

import com.almuradev.droplet.registry.Registry;
import com.almuradev.toolbox.inject.ToolboxBinder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import net.kyori.violet.AbstractModule;
import net.kyori.violet.FriendlyTypeLiteral;
import net.kyori.violet.TypeArgument;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.color.ColorType;
import org.inspirenxe.skills.api.effect.firework.FireworkEffectType;
import org.inspirenxe.skills.api.effect.potion.PotionEffectType;
import org.inspirenxe.skills.api.effect.sound.SoundEffectType;
import org.inspirenxe.skills.api.function.economy.EconomyFunction;
import org.inspirenxe.skills.api.function.level.LevelFunction;
import org.inspirenxe.skills.api.sound.SoundEffect;
import org.inspirenxe.skills.impl.registry.module.ColorTypeRegistryModule;
import org.inspirenxe.skills.impl.registry.module.EconomyFunctionRegistryModule;
import org.inspirenxe.skills.impl.registry.module.FireworkEffectTypeRegistryModule;
import org.inspirenxe.skills.impl.registry.module.LevelFunctionRegistryModule;
import org.inspirenxe.skills.impl.registry.module.PotionEffectTypeRegistryModule;
import org.inspirenxe.skills.impl.registry.module.SkillTypeRegistryModule;
import org.inspirenxe.skills.impl.registry.module.SoundEffectTypeRegistryModule;
import org.inspirenxe.skills.impl.sound.SoundEffectBuilderImpl;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.ItemType;

public final class RegistryModule extends AbstractModule implements ToolboxBinder {

  @Override
  protected void configure() {
    // TODO Move this to common...
    this.registry().builder(SoundEffect.Builder.class, SoundEffectBuilderImpl::new);

    // API modules
    this.registry().module(ColorType.class, ColorTypeRegistryModule.instance);
    this.registry().module(EconomyFunction.class, EconomyFunctionRegistryModule.instance);
    this.registry().module(FireworkEffectType.class, FireworkEffectTypeRegistryModule.instance);
    this.registry().module(LevelFunction.class, LevelFunctionRegistryModule.instance);
    this.registry().module(PotionEffectType.class, PotionEffectTypeRegistryModule.instance);
    this.registry().module(SkillType.class, SkillTypeRegistryModule.instance);
    this.registry().module(SoundEffectType.class, SoundEffectTypeRegistryModule.instance);

    // Registry binders
    this.bindRegistry(BlockType.class).to(new TypeLiteral<CatalogTypeRegistry<BlockType>>() {
    });
    this.bindRegistry(ColorType.class).to(new TypeLiteral<CatalogTypeRegistry<ColorType>>() {
    });
    this.bindRegistry(EconomyFunction.class).to(new TypeLiteral<CatalogTypeRegistry<EconomyFunction>>() {
    });
    this.bindRegistry(FireworkEffectType.class).to(new TypeLiteral<CatalogTypeRegistry<FireworkEffectType>>() {
    });
    this.bindRegistry(FireworkShape.class).to(new TypeLiteral<CatalogTypeRegistry<FireworkShape>>() {
    });
    this.bindRegistry(ItemType.class).to(new TypeLiteral<CatalogTypeRegistry<ItemType>>() {
    });
    this.bindRegistry(Key.class).to(new TypeLiteral<CatalogTypeRegistry<Key>>() {
    });
    this.bindRegistry(LevelFunction.class).to(new TypeLiteral<CatalogTypeRegistry<LevelFunction>>() {
    });
    // TODO API interface name is retarded, should be PotionType. Fix in API 8.
    this.bindRegistry(org.spongepowered.api.effect.potion.PotionEffectType.class).to(new TypeLiteral<CatalogTypeRegistry<org.spongepowered.api
        .effect.potion.PotionEffectType>>() {
    });
    this.bindRegistry(PotionEffectType.class).to(new TypeLiteral<CatalogTypeRegistry<PotionEffectType>>() {
    });
    this.bindRegistry(SkillType.class).to(new TypeLiteral<CatalogTypeRegistry<SkillType>>() {
    });
    this.bindRegistry(SoundCategory.class).to(new TypeLiteral<CatalogTypeRegistry<SoundCategory>>() {
    });
    this.bindRegistry(SoundEffectType.class).to(new TypeLiteral<CatalogTypeRegistry<SoundEffectType>>() {
    });
    this.bindRegistry(SoundType.class).to(new TypeLiteral<CatalogTypeRegistry<SoundType>>() {
    });
  }

  private <T> LinkedBindingBuilder<Registry<T>> bindRegistry(final Class<T> type) {
    return this.bind(new FriendlyTypeLiteral<Registry<T>>() {
    }.where(new TypeArgument<T>(type) {
    }));
  }
}
