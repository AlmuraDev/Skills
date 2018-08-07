package org.inspirenxe.skills.impl.content.component.apply.data;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Injector;
import net.kyori.violet.FriendlyTypeLiteral;
import net.kyori.violet.TypeArgument;
import org.inspirenxe.skills.impl.content.parser.value.StringToValueParser;
import org.spongepowered.api.data.key.Key;

import javax.annotation.Nullable;

public class KeyValue {

    @Inject private static Injector injector;

    private final Key<?> key;
    @Nullable private final Object value;

    public KeyValue(Key<?> key, String rawValue) {
        this.key = key;
        if (rawValue != null) {
            final StringToValueParser<?> parser = this.getParserInstance(key.getElementToken());
            this.value = parser.parse(key.getElementToken(), rawValue).orElse(null);
        } else {
            this.value = null;
        }
    }


    // This MUST be in its own method for the runtime generics magic to work properly
    private <T> StringToValueParser<T> getParserInstance(TypeToken<T> token) {
        return injector.getInstance(com.google.inject.Key.get(new FriendlyTypeLiteral<StringToValueParser<T>>() {

        }.where(new TypeArgument<T>(token) {
        })));
    }

    public Key<?> getKey() {
        return this.key;
    }


    @Nullable public Object getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "KeyValue{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }

}
