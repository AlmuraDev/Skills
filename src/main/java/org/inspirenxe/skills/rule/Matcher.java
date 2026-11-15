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
package org.inspirenxe.skills.rule;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.api.tag.DefaultedTag;
import org.spongepowered.api.tag.Taggable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Matches blocks or items by an entry of a rule:
 *
 * <ul>
 *     <li>{@code minecraft:stone}: that block or item</li>
 *     <li>{@code minecraft:wheat[age=7]}: that block with these properties (properties not listed may have any value)</li>
 *     <li>{@code #minecraft:logs}: every block or item in that tag</li>
 *     <li>{@code almura:*}: every block or item of that namespace</li>
 * </ul>
 */
public abstract sealed class Matcher<T> permits Matcher.Block, Matcher.Item {

    final String entry;

    Matcher(final String entry) {
        this.entry = entry;
    }

    /**
     * @return True if a value of this type could match (used to index rules by type)
     */
    abstract boolean couldMatch(T type);

    @Override
    public String toString() {
        return this.entry;
    }

    /**
     * Parses one entry, resolving it against the registries.
     *
     * @throws IllegalArgumentException If the entry names an unknown block, item, tag or property
     */
    static Matcher<?> parse(final String entry, final boolean blocks) {
        final String trimmed = entry.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Empty entry");
        }
        if (blocks) {
            return Block.parse(trimmed);
        }
        if (trimmed.indexOf('[') >= 0) {
            throw new IllegalArgumentException("Items have no properties: '" + trimmed + "'");
        }
        return new Item(trimmed, Selection.parse(trimmed, RegistryTypes.ITEM_TYPE));
    }

    /**
     * The types an entry selects: one type, a tag or a namespace.
     */
    record Selection<T>(DefaultedRegistryType<T> registry, T type, DefaultedTag<T> tag, String namespace) {

        static <T> Selection<T> parse(final String id, final DefaultedRegistryType<T> registry) {
            if (id.startsWith("#")) {
                final ResourceKey key = ResourceKey.resolve(id.substring(1));
                final DefaultedTag<T> tag = DefaultedTag.of(registry, key);
                return new Selection<>(registry, null, tag, null);
            }
            if (id.endsWith(":*")) {
                final String namespace = id.substring(0, id.length() - 2);
                if (namespace.isEmpty()) {
                    throw new IllegalArgumentException("Missing namespace in '" + id + "'");
                }
                return new Selection<>(registry, null, null, namespace);
            }
            final ResourceKey key = ResourceKey.resolve(id);
            final T type = registry.get().<T>findValue(key).orElseThrow(() -> new IllegalArgumentException("Unknown id '" + id + "'"));
            return new Selection<>(registry, type, null, null);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        boolean contains(final T value) {
            if (this.type != null) {
                return this.type == value;
            }
            if (this.tag != null) {
                return value instanceof Taggable taggable && taggable.is((DefaultedTag) this.tag);
            }
            return this.registry.get().findValueKey(value).map(key -> key.namespace().equals(this.namespace)).orElse(false);
        }
    }

    public static final class Block extends Matcher<BlockType> {

        private final Selection<BlockType> selection;
        // Property name to the wanted value, compared by the property's own value names
        private final Map<String, String> properties;

        private Block(final String entry, final Selection<BlockType> selection, final Map<String, String> properties) {
            super(entry);
            this.selection = selection;
            this.properties = properties;
        }

        static Block parse(final String entry) {
            final int open = entry.indexOf('[');
            final String id = open < 0 ? entry : entry.substring(0, open);
            final Map<String, String> properties = new HashMap<>();
            if (open >= 0) {
                if (!entry.endsWith("]")) {
                    throw new IllegalArgumentException("Missing ']' in '" + entry + "'");
                }
                for (final String pair : entry.substring(open + 1, entry.length() - 1).split(",")) {
                    if (pair.isBlank()) {
                        continue;
                    }
                    final int equals = pair.indexOf('=');
                    if (equals <= 0) {
                        throw new IllegalArgumentException("Property '" + pair + "' in '" + entry + "' needs name=value");
                    }
                    properties.put(pair.substring(0, equals).trim(), pair.substring(equals + 1).trim());
                }
            }
            final Selection<BlockType> selection = Selection.parse(id, RegistryTypes.BLOCK_TYPE);
            if (selection.type() != null && !properties.isEmpty()) {
                // Check the properties exist and the values parse, so typos fail while loading instead of never matching
                final Set<String> names = selection.type().stateProperties().stream().map(StateProperty::name).collect(Collectors.toSet());
                for (final Map.Entry<String, String> property : properties.entrySet()) {
                    final StateProperty<?> stateProperty = selection.type().stateProperties().stream()
                            .filter(p -> p.name().equals(property.getKey()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Block '" + id + "' has no property '" + property.getKey()
                                    + "', it has " + names));
                    if (stateProperty.parseValue(property.getValue()).isEmpty()) {
                        throw new IllegalArgumentException("'" + property.getValue() + "' is no value of property '" + property.getKey()
                                + "' of block '" + id + "'");
                    }
                }
            }
            return new Block(entry, selection, Map.copyOf(properties));
        }

        @Override
        boolean couldMatch(final BlockType type) {
            return this.selection.contains(type);
        }

        public boolean matches(final BlockState state) {
            if (!this.selection.contains(state.type())) {
                return false;
            }
            if (this.properties.isEmpty()) {
                return true;
            }
            for (final Map.Entry<StateProperty<?>, ?> entry : state.statePropertyMap().entrySet()) {
                final String wanted = this.properties.get(entry.getKey().name());
                if (wanted != null && !Matcher.Block.valueEquals(entry.getKey(), entry.getValue(), wanted)) {
                    return false;
                }
            }
            // A property listed that the block doesn't have (only possible for tags and namespaces) never matches
            for (final String name : this.properties.keySet()) {
                if (state.statePropertyMap().keySet().stream().noneMatch(p -> p.name().equals(name))) {
                    return false;
                }
            }
            return true;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static boolean valueEquals(final StateProperty property, final Object value, final String wanted) {
            final java.util.Optional<?> parsed = property.parseValue(wanted);
            return parsed.isPresent() && Objects.equals(parsed.get(), value);
        }
    }

    public static final class Item extends Matcher<ItemType> {

        private final Selection<ItemType> selection;

        private Item(final String entry, final Selection<ItemType> selection) {
            super(entry);
            this.selection = selection;
        }

        @Override
        boolean couldMatch(final ItemType type) {
            return this.selection.contains(type);
        }

        public boolean matches(final ItemType type) {
            return this.selection.contains(type);
        }
    }
}
