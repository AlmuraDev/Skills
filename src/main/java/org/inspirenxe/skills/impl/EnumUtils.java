package org.inspirenxe.skills.impl;

import org.inspirenxe.skills.impl.content.parser.lazy.block.BlockTransactionSource;

import java.util.Optional;

public class EnumUtils {

    public static <T extends Enum<T>> Optional<T> parse(Class<T> enumClass, String name) {
        try {
            return Optional.of(Enum.valueOf(enumClass, name.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

}
