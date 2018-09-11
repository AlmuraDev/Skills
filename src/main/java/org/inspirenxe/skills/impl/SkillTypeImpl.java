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
package org.inspirenxe.skills.impl;

import com.almuradev.droplet.content.type.Content;
import com.almuradev.droplet.registry.RegistryKey;
import com.almuradev.droplet.registry.reference.RegistryReference;
import com.google.common.base.MoreObjects;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.function.economy.EconomyFunctionType;
import org.inspirenxe.skills.api.function.level.LevelFunctionType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Objects;
import java.util.Optional;

public final class SkillTypeImpl implements SkillType, Content {

    private final RegistryKey registryKey;
    private final String name;
    private final Text formattedName;
    private final RegistryReference<LevelFunctionType> levelFunction;
    private final RegistryReference<EconomyFunctionType> economyFunction;
    private final int maxLevel;

    public SkillTypeImpl(final RegistryKey registryKey, final String name, final RegistryReference<LevelFunctionType> levelFunction,
        final RegistryReference<EconomyFunctionType> economyFunction, final int maxLevel) {
        this.registryKey = registryKey;
        this.name = TextSerializers.FORMATTING_CODE.stripCodes(name);
        this.formattedName = TextSerializers.FORMATTING_CODE.deserialize(name);
        this.levelFunction = levelFunction;
        this.economyFunction = economyFunction;
        this.maxLevel = maxLevel;
    }

    @Override
    public String getId() {
        return this.registryKey.toString();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Text getFormattedName() {
        return this.formattedName;
    }

    @Override
    public LevelFunctionType getLevelFunction() {
        return this.levelFunction.require();
    }

    @Override
    public Optional<EconomyFunctionType> getEconomyFunction() {
        return Optional.ofNullable(this.economyFunction.get());
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SkillTypeImpl)) {
            return false;
        }
        final SkillTypeImpl skillType = (SkillTypeImpl) o;
        return Objects.equals(this.registryKey, skillType.registryKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.registryKey);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.registryKey)
                .add("levelFunction", this.levelFunction.require())
                .add("economyFunction", this.economyFunction.get())
                .add("maxLevel", this.maxLevel)
                .toString();
    }
}
