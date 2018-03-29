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
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import net.kyori.violet.AbstractModule;
import net.kyori.violet.FriendlyTypeLiteral;
import net.kyori.violet.TypeArgument;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.function.economy.EconomyFunction;
import org.inspirenxe.skills.api.function.level.LevelFunction;
import org.inspirenxe.skills.api.effect.firework.FireworkEffectType;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.FireworkShape;
import org.spongepowered.api.item.ItemType;

public final class RegistryModule extends AbstractModule {

  @Override
  protected void configure() {
    this.bindRegistry(BlockType.class).to(new TypeLiteral<CatalogTypeRegistry<BlockType>>() {});
    this.bindRegistry(ItemType.class).to(new TypeLiteral<CatalogTypeRegistry<ItemType>>() {});
    this.bindRegistry(FireworkEffectType.class).to(new TypeLiteral<CatalogTypeRegistry<FireworkEffectType>>() {});
    this.bindRegistry(FireworkShape.class).to(new TypeLiteral<CatalogTypeRegistry<FireworkShape>>() {});

    this.bindRegistry(LevelFunction.class).to(new TypeLiteral<CatalogTypeRegistry<LevelFunction>>() {});
    this.bindRegistry(EconomyFunction.class).to(new TypeLiteral<CatalogTypeRegistry<EconomyFunction>>() {});
    this.bindRegistry(SkillType.class).to(new TypeLiteral<CatalogTypeRegistry<SkillType>>() {});
  }

  private <T> LinkedBindingBuilder<Registry<T>> bindRegistry(final Class<T> type) {
    return this.bind(new FriendlyTypeLiteral<Registry<T>>() {}.where(new TypeArgument<T>(type) {}));
  }
}
