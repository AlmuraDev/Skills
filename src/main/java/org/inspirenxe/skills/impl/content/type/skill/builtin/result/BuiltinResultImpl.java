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
package org.inspirenxe.skills.impl.content.type.skill.builtin.result;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.Chain;
import org.inspirenxe.skills.impl.result.experience.ExperienceResultImpl;

import java.math.BigDecimal;
import java.util.Optional;

import javax.annotation.Nullable;

public final class BuiltinResultImpl extends ExperienceResultImpl implements BuiltinResult {

    @Nullable private Skill skill;
    @Nullable private Chain<?> chain;
    @Nullable private BigDecimal money;

    public BuiltinResultImpl(final BuiltinResultBuilder builder) {
        super(builder);
        this.skill = checkNotNull(builder.skill);
        this.chain = builder.chain;
        this.money = builder.money;
    }

    @Override
    public Skill getSkill() {
        return this.skill;
    }

    @Override
    public Optional<Chain<?>> getChain() {
        return Optional.ofNullable(this.chain);
    }

    @Override
    public Optional<BigDecimal> getMoney() {
        return Optional.ofNullable(this.money);
    }
}
