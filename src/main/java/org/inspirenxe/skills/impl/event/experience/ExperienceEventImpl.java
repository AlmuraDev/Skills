/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE <https://github.com/InspireNXE/>
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
package org.inspirenxe.skills.impl.event.experience;

import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.spongepowered.api.event.cause.Cause;

public abstract class ExperienceEventImpl implements ExperienceEvent {

    private final Cause cause;
    private final SkillType skillType;
    private final double originalExperience;
    protected double experience;

    protected ExperienceEventImpl(Cause cause, SkillType skillType, double originalExperience, double
            experience) {
        this.cause = cause;
        this.skillType = skillType;
        this.originalExperience = originalExperience;
        this.experience = experience;
    }

    @Override
    public final double getOriginalExperience() {
        return this.originalExperience;
    }

    @Override
    public final double getExperience() {
        return this.experience;
    }

    @Override
    public SkillType getTargetSkillType() {
        return this.skillType;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }
}
