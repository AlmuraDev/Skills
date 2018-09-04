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
package org.inspirenxe.skills.impl.content.type.skill.builtin;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.impl.event.experience.AbstractExperienceResultBuilder;

import java.math.BigDecimal;

import javax.annotation.Nullable;

public final class BuiltinResultBuilder extends AbstractExperienceResultBuilder<BuiltinResult, BuiltinResult.Builder> implements BuiltinResult.Builder {

    @Nullable public Skill skill;
    @Nullable public ChainBuilder<?> chain;
    @Nullable public BigDecimal money;

    @Override
    public BuiltinResult.Builder skill(final Skill skill) {
        this.skill = skill;
        return this;
    }

    @Override
    public BuiltinResult.Builder chain(final ChainBuilder<?> chain) {
        this.chain = chain;
        return this;
    }

    @Override
    public BuiltinResult.Builder money(final BigDecimal money) {
        this.money = money;
        return this;
    }

    @Override
    public BuiltinResultBuilder reset() {
        this.type = null;
        this.ex = null;
        this.xp = null;
        this.money = null;
        return this;
    }

    @Override
    public BuiltinResultBuilder from(final BuiltinResult value) {
        this.type = value.getType();
        this.xp = value.getXp().orElse(null);
        this.money = value.getMoney().orElse(null);
        this.skill = null;
        this.chain = null;
        return this;
    }

    @Override
    public BuiltinResult build() {
        return new BuiltinResultImpl(this);
    }
}
