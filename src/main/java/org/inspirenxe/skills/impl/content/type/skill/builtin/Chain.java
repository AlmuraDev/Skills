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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.impl.util.function.TriConsumer;
import org.spongepowered.api.entity.living.player.Player;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public abstract class Chain<B extends Chain<B>> {

    public Integer level;
    public Double xp;
    public Double economy;
    public TriConsumer<Player, Skill, Integer> denyLevelRequired;

    protected boolean inErrorState = false;

    public B level(final Integer value) {
        if (value != null) {
            checkState(value >= 0);
        }

        this.level = value;
        return (B) this;
    }

    public B xp(final Double value) {
        this.xp = value;
        return (B) this;
    }

    public B economy(final Double value) {
        this.economy = value;
        return (B) this;
    }

    public B denyLevelRequired(@Nullable final TriConsumer<Player, Skill, Integer> value) {
        if (this.inErrorState) {
            return (B) this;
        }
        this.denyLevelRequired = value;
        return (B) this;
    }

    public B from(final B builder) {
        if (this.inErrorState) {
            return (B) this;
        }

        checkNotNull(builder);

        this.level = builder.level;
        this.xp = builder.xp;
        this.economy = builder.economy;
        this.denyLevelRequired = builder.denyLevelRequired;

        return (B) this;
    }
}