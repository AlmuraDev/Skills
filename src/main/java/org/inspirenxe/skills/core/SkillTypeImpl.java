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
package org.inspirenxe.skills.core;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.function.EconomyFunction;
import org.inspirenxe.skills.api.function.LevelFunction;
import org.spongepowered.api.ResourceKey;

import java.util.Optional;

public final class SkillTypeImpl implements SkillType {

    private final ResourceKey key;
    private final String name;
    private final Component formattedName;
    private final LevelFunction levelFunction;
    private final EconomyFunction economyFunction;
    private final int maxLevel;

    /**
     * @param name The name with {@code &} color codes, e.g. {@code &7Mining}
     */
    public SkillTypeImpl(final ResourceKey key, final String name, final LevelFunction levelFunction, final EconomyFunction economyFunction,
            final int maxLevel) {
        this.key = key;
        this.formattedName = LegacyComponentSerializer.legacyAmpersand().deserialize(name);
        this.name = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(this.formattedName);
        this.levelFunction = levelFunction;
        this.economyFunction = economyFunction;
        this.maxLevel = maxLevel;
    }

    @Override
    public ResourceKey key() {
        return this.key;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Component formattedName() {
        return this.formattedName;
    }

    @Override
    public LevelFunction levelFunction() {
        return this.levelFunction;
    }

    @Override
    public Optional<EconomyFunction> economyFunction() {
        return Optional.ofNullable(this.economyFunction);
    }

    @Override
    public int maxLevel() {
        return this.maxLevel;
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof SkillTypeImpl other && this.key.equals(other.key);
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return "SkillType{" + this.key + "}";
    }
}
