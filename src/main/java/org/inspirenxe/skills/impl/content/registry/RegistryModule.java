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
package org.inspirenxe.skills.impl.content.registry;

import com.almuradev.droplet.registry.Registry;
import com.almuradev.toolbox.inject.ToolboxBinder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import net.kyori.violet.AbstractModule;
import net.kyori.violet.FriendlyTypeLiteral;
import net.kyori.violet.TypeArgument;
import org.inspirenxe.skills.api.result.Result;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.color.ColorType;
import org.inspirenxe.skills.api.effect.firework.FireworkEffectType;
import org.inspirenxe.skills.api.effect.potion.PotionEffectType;
import org.inspirenxe.skills.api.effect.sound.SoundEffectType;
import org.inspirenxe.skills.api.result.experience.ExperienceResult;
import org.inspirenxe.skills.api.function.economy.EconomyFunctionType;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.inspirenxe.skills.api.sound.SoundEffect;
import org.inspirenxe.skills.impl.result.ResultBuilder;
import org.inspirenxe.skills.impl.content.registry.module.ColorTypeRegistryModule;
import org.inspirenxe.skills.impl.content.registry.module.EconomyFunctionRegistryModule;
import org.inspirenxe.skills.impl.content.registry.module.FireworkEffectTypeRegistryModule;
import org.inspirenxe.skills.impl.content.registry.module.LevelFunctionRegistryModule;
import org.inspirenxe.skills.impl.content.registry.module.PotionEffectTypeRegistryModule;
import org.inspirenxe.skills.impl.content.registry.module.SkillTypeRegistryModule;
import org.inspirenxe.skills.impl.content.registry.module.SoundEffectTypeRegistryModule;
import org.inspirenxe.skills.impl.content.type.skill.builtin.result.BuiltinResult;
import org.inspirenxe.skills.impl.content.type.skill.builtin.result.BuiltinResultBuilder;
import org.inspirenxe.skills.impl.result.experience.ExperienceResultBuilder;
import org.inspirenxe.skills.impl.sound.SoundEffectBuilderImpl;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.item.ItemType;

public final class RegistryModule extends AbstractModule implements ToolboxBinder {

  @Override
  protected void configure() {
    this.registry().builder(SoundEffect.Builder.class, SoundEffectBuilderImpl::new);
    this.registry().builder(Result.Builder.class, ResultBuilder::new);
    this.registry().builder(ExperienceResult.DirectBuilder.class, ExperienceResultBuilder::new);
    this.registry().builder(BuiltinResult.Builder.class, BuiltinResultBuilder::new);

    // API modules
    this.registry().module(ColorType.class, new ColorTypeRegistryModule());
    this.registry().module(EconomyFunctionType.class, new EconomyFunctionRegistryModule());
    this.registry().module(FireworkEffectType.class, new FireworkEffectTypeRegistryModule());
    this.registry().module(LevelFunctionType.class, new LevelFunctionRegistryModule());
    this.registry().module(PotionEffectType.class, new PotionEffectTypeRegistryModule());
    this.registry().module(SkillType.class, new SkillTypeRegistryModule());
    this.registry().module(SoundEffectType.class, new SoundEffectTypeRegistryModule());

    // Registry binders
    this.bindRegistry(BlockType.class).to(new TypeLiteral<CatalogTypeRegistry<BlockType>>() {});
    this.bindRegistry(ColorType.class).to(new TypeLiteral<CatalogTypeRegistry<ColorType>>() {});
    this.bindRegistry(EconomyFunctionType.class).to(new TypeLiteral<CatalogTypeRegistry<EconomyFunctionType>>() {});
    this.bindRegistry(FireworkEffectType.class).to(new TypeLiteral<CatalogTypeRegistry<FireworkEffectType>>() {});
    this.bindRegistry(ItemType.class).to(new TypeLiteral<CatalogTypeRegistry<ItemType>>() {});
    this.bindRegistry(Key.class).to(new TypeLiteral<CatalogTypeRegistry<Key>>() {});
    this.bindRegistry(LevelFunctionType.class).to(new TypeLiteral<CatalogTypeRegistry<LevelFunctionType>>() {});
    this.bindRegistry(PotionEffectType.class).to(new TypeLiteral<CatalogTypeRegistry<PotionEffectType>>() {});
    this.bindRegistry(SkillType.class).to(new TypeLiteral<CatalogTypeRegistry<SkillType>>() {});
    this.bindRegistry(SoundEffectType.class).to(new TypeLiteral<CatalogTypeRegistry<SoundEffectType>>() {});
  }

  private <T> LinkedBindingBuilder<Registry<T>> bindRegistry(final Class<T> type) {
    return this.bind(new FriendlyTypeLiteral<Registry<T>>() {}.where(new TypeArgument<T>(type) {}));
  }
}
