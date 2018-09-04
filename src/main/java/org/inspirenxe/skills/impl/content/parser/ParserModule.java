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
package org.inspirenxe.skills.impl.content.parser;

import com.almuradev.droplet.registry.RegistryKey;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import net.kyori.violet.AbstractModule;
import net.kyori.violet.FriendlyTypeLiteral;
import net.kyori.violet.TypeArgument;
import net.kyori.xml.node.parser.EnumParser;
import net.kyori.xml.node.parser.ParserBinder;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.inspirenxe.skills.impl.cause.CauseOperatorType;
import org.inspirenxe.skills.impl.cause.CauseType;
import org.inspirenxe.skills.impl.content.component.apply.EventApplicatorImpl;
import org.inspirenxe.skills.impl.content.component.apply.MathOperationType;
import org.inspirenxe.skills.impl.content.component.apply.data.KeyValue;
import org.inspirenxe.skills.impl.content.component.apply.data.KeyValueParser;
import org.inspirenxe.skills.impl.content.component.apply.math.BigDecimalParser;
import org.inspirenxe.skills.impl.content.component.apply.math.MathOperation;
import org.inspirenxe.skills.impl.content.component.apply.math.MathOperationParser;
import org.inspirenxe.skills.impl.content.component.apply.message.Message;
import org.inspirenxe.skills.impl.content.component.apply.message.MessageParser;
import org.inspirenxe.skills.impl.content.parser.lazy.block.BlockTransactionSource;
import org.inspirenxe.skills.impl.content.parser.lazy.block.LazyBlockState;
import org.inspirenxe.skills.impl.content.parser.lazy.block.LazyBlockStateParser;
import org.inspirenxe.skills.impl.content.parser.lazy.block.value.LazyStateValue;
import org.inspirenxe.skills.impl.content.parser.lazy.block.value.LazyStateValueParser;
import org.inspirenxe.skills.impl.content.parser.lazy.item.LazyItemStack;
import org.inspirenxe.skills.impl.content.parser.lazy.item.LazyItemStackParser;
import org.inspirenxe.skills.impl.content.parser.value.CatalogStringToValueParser;
import org.inspirenxe.skills.impl.content.parser.value.PrimitiveStringToValueParser;
import org.inspirenxe.skills.impl.content.parser.value.StringToValueParser;
import org.inspirenxe.skills.impl.database.DatabaseConfiguration;
import org.inspirenxe.skills.impl.database.DatabaseConfigurationParser;
import org.jooq.SQLDialect;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.item.FireworkShape;

import java.math.BigDecimal;

public final class ParserModule extends AbstractModule {

  @Override
  protected void configure() {

    final ParserBinder parsers = new ParserBinder(this.binder());
    parsers.bindParser(RegistryKey.class).to(RegistryKeyParser.class);
    parsers.bindParser(SQLDialect.class).to(new TypeLiteral<EnumParser<SQLDialect>>() {});
    parsers.bindParser(DatabaseConfiguration.class).to(DatabaseConfigurationParser.class);
    parsers.bindParser(LevelFunctionType.class).to(new TypeLiteral<CatalogTypeParser<LevelFunctionType>>() {});
    parsers.bindParser(SoundCategory.class).to(new TypeLiteral<CatalogTypeParser<SoundCategory>>() {});
    parsers.bindParser(SoundType.class).to(new TypeLiteral<CatalogTypeParser<SoundType>>() {});
    parsers.bindParser(PotionEffectType.class).to(new TypeLiteral<CatalogTypeParser<PotionEffectType>>() {});
    parsers.bindParser(FireworkShape.class).to(new TypeLiteral<CatalogTypeParser<FireworkShape>>() {});
    parsers.bindParser(LazyBlockState.class).to(LazyBlockStateParser.class);
    parsers.bindParser(LazyItemStack.class).to(LazyItemStackParser.class);
    parsers.bindParser(new TypeLiteral<LazyStateValue<?>>() {}).to(LazyStateValueParser.class);
    parsers.bindParser(BlockTransactionSource.class).to(new TypeLiteral<EnumParser<BlockTransactionSource>>() {});

//    parsers.bindParser(CauseOperatorType.class).to(new TypeLiteral<EnumParser<CauseOperatorType>>() {});
//    parsers.bindParser(CauseType.class).to(new TypeLiteral<EnumParser<CauseType>>() {});
//    parsers.bindParser(MathOperationType.class).to(new TypeLiteral<EnumParser<MathOperationType>>() {});
//    parsers.bindParser(BigDecimal.class).to(BigDecimalParser.class);
//    parsers.bindParser(MathOperation.class).to(MathOperationParser.class);
//    parsers.bindParser(Message.class).to(MessageParser.class);
//    parsers.bindParser(KeyValue.class).to(KeyValueParser.class);

    // Commence Hacks
    // TODO Add hackery parsers as I need them
    this.bindRawParser(Boolean.class).to(new TypeLiteral<PrimitiveStringToValueParser<Boolean>>() {});
    this.bindRawParser(String.class).to(new TypeLiteral<PrimitiveStringToValueParser<String>>() {});
    this.bindRawParser(Integer.class).to(new TypeLiteral<PrimitiveStringToValueParser<Integer>>() {});
    this.bindRawParser(GameMode.class).to(new TypeLiteral<CatalogStringToValueParser<GameMode>>() {});
  }

  private <T> LinkedBindingBuilder<StringToValueParser<T>> bindRawParser(final Class<T> type) {
    return this.bind(new FriendlyTypeLiteral<StringToValueParser<T>>() {}.where(new TypeArgument<T>(type) {}));
  }
}
