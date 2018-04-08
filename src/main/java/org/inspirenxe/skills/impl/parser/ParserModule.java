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
package org.inspirenxe.skills.impl.parser;

import com.almuradev.droplet.parser.EnumParser;
import com.almuradev.droplet.parser.ParserBinder;
import com.almuradev.droplet.registry.RegistryKey;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import net.kyori.violet.AbstractModule;
import net.kyori.violet.FriendlyTypeLiteral;
import net.kyori.violet.TypeArgument;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.inspirenxe.skills.impl.cause.CauseOperatorType;
import org.inspirenxe.skills.impl.cause.CauseType;
import org.inspirenxe.skills.impl.database.DatabaseConfiguration;
import org.inspirenxe.skills.impl.database.DatabaseConfigurationParser;
import org.inspirenxe.skills.impl.parser.lazy.block.LazyBlockState;
import org.inspirenxe.skills.impl.parser.lazy.block.LazyBlockStateParser;
import org.inspirenxe.skills.impl.parser.lazy.block.value.LazyStateValue;
import org.inspirenxe.skills.impl.parser.lazy.block.value.LazyStateValueParser;
import org.inspirenxe.skills.impl.parser.lazy.item.LazyItemStack;
import org.inspirenxe.skills.impl.parser.lazy.item.LazyItemStackParser;
import org.inspirenxe.skills.impl.parser.value.CatalogStringToValueParser;
import org.inspirenxe.skills.impl.parser.value.PrimitiveStringToValueParser;
import org.inspirenxe.skills.impl.parser.value.StringToValueParser;
import org.jooq.SQLDialect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.item.FireworkShape;

public final class ParserModule extends AbstractModule implements ParserBinder {

  @Override
  protected void configure() {
    this.bindParser(CauseOperatorType.class).to(new TypeLiteral<EnumParser<CauseOperatorType>>() {});
    this.bindParser(CauseType.class).to(new TypeLiteral<EnumParser<CauseType>>() {});
    this.bindParser(DatabaseConfiguration.class).to(DatabaseConfigurationParser.class);
    this.bindParser(FireworkShape.class).to(new TypeLiteral<CatalogTypeParser<FireworkShape>>() {});
    this.bindParser(LazyBlockState.class).to(LazyBlockStateParser.class);
    this.bindParser(new TypeLiteral<LazyStateValue<?>>() {}).to(LazyStateValueParser.class);
    this.bindParser(LazyItemStack.class).to(LazyItemStackParser.class);
    this.bindParser(LevelFunctionType.class).to(new TypeLiteral<CatalogTypeParser<LevelFunctionType>>() {});
    this.bindParser(PotionEffectType.class).to(new TypeLiteral<CatalogTypeParser<PotionEffectType>>() {});
    this.bindParser(RegistryKey.class).to(RegistryKeyParser.class);
    this.bindParser(SQLDialect.class).to(new TypeLiteral<EnumParser<SQLDialect>>() {});
    this.bindParser(SoundCategory.class).to(new TypeLiteral<CatalogTypeParser<SoundCategory>>() {});
    this.bindParser(SoundType.class).to(new TypeLiteral<CatalogTypeParser<SoundType>>() {});

    // Commence Hacks
    // TODO Add hackery parsers as I need them
    this.bindRawParser(Boolean.class).to(new TypeLiteral<PrimitiveStringToValueParser<Boolean>>() {});
    this.bindRawParser(String.class).to(new TypeLiteral<PrimitiveStringToValueParser<String>>() {});
    this.bindRawParser(GameMode.class).to(new TypeLiteral<CatalogStringToValueParser<GameMode>>() {});
  }

  private <T> LinkedBindingBuilder<StringToValueParser<T>> bindRawParser(final Class<T> type) {
    return this.bind(new FriendlyTypeLiteral<StringToValueParser<T>>() {}.where(new TypeArgument<T>(type) {}));
  }
}
