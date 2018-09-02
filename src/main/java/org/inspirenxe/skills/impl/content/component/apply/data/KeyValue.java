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
package org.inspirenxe.skills.impl.content.component.apply.data;

import com.google.common.base.MoreObjects;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import net.kyori.violet.FriendlyTypeLiteral;
import net.kyori.violet.TypeArgument;
import org.inspirenxe.skills.impl.content.parser.value.StringToValueParser;
import org.spongepowered.api.data.key.Key;

import javax.annotation.Nullable;

public final class KeyValue {

    @Inject private static Injector injector;

    private final Key<?> key;
    @Nullable private final Object value;

    public KeyValue(final Key<?> key, final String rawValue) {
        this.key = key;
        if (rawValue != null) {
            final StringToValueParser<?> parser = this.getParserInstance(key.getElementToken());
            this.value = parser.parse(key.getElementToken(), rawValue).orElse(null);
        } else {
            this.value = null;
        }
    }


    // This MUST be in its own method for the runtime generics magic to work properly
    private <T> StringToValueParser<T> getParserInstance(final TypeToken<T> token) {
        return injector.getInstance(com.google.inject.Key.get(new FriendlyTypeLiteral<StringToValueParser<T>>() {

        }.where(new TypeArgument<T>(token) {
        })));
    }

    public Key<?> getKey() {
        return this.key;
    }


    @Nullable
    public Object getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
          .add("key", this.key)
          .add("value", this.value)
          .toString();
    }

}
