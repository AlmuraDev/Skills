package org.inspirenxe.skills.impl.content.component.apply.data;

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
            final StringToValueParser<?> parser = injector.getInstance(com.google.inject.Key.get(new FriendlyTypeLiteral<StringToValueParser<?>>() {

            }.where(new TypeArgument(key.getElementToken()) {
            })));
            this.value = parser.parse(key.getElementToken(), rawValue).orElse(null);
        } else {
            this.value = null;
        }
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
