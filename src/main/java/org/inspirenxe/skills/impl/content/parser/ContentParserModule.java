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
import net.kyori.violet.AbstractModule;
import net.kyori.xml.node.parser.ParserBinder;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.spongepowered.api.effect.potion.PotionEffectType;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.item.FireworkShape;

public final class ContentParserModule extends AbstractModule {

    @Override
    protected void configure() {

        final ParserBinder parsers = new ParserBinder(this.binder());
        parsers.bindParser(RegistryKey.class).to(RegistryKeyParser.class);
        parsers.bindParser(LevelFunctionType.class).to(new TypeLiteral<CatalogTypeParser<LevelFunctionType>>() {
        });
        parsers.bindParser(SoundCategory.class).to(new TypeLiteral<CatalogTypeParser<SoundCategory>>() {
        });
        parsers.bindParser(SoundType.class).to(new TypeLiteral<CatalogTypeParser<SoundType>>() {
        });
        parsers.bindParser(PotionEffectType.class).to(new TypeLiteral<CatalogTypeParser<PotionEffectType>>() {
        });
        parsers.bindParser(FireworkShape.class).to(new TypeLiteral<CatalogTypeParser<FireworkShape>>() {
        });
    }
}
