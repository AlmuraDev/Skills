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

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.event.ChangeExperienceEvent;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

final class ExperienceEvents {

    private ExperienceEvents() {
    }

    abstract static class Base extends AbstractEvent implements ChangeExperienceEvent {

        private final Cause cause;
        private final Skill skill;
        private final double originalExperience;

        Base(final Cause cause, final Skill skill, final double originalExperience) {
            this.cause = cause;
            this.skill = skill;
            this.originalExperience = originalExperience;
        }

        @Override
        public Cause cause() {
            return this.cause;
        }

        @Override
        public Skill skill() {
            return this.skill;
        }

        @Override
        public double originalExperience() {
            return this.originalExperience;
        }
    }

    static final class Pre extends Base implements ChangeExperienceEvent.Pre {

        private double experience;
        private boolean cancelled;

        Pre(final Cause cause, final Skill skill, final double originalExperience, final double experience) {
            super(cause, skill, originalExperience);
            this.experience = experience;
        }

        @Override
        public double experience() {
            return this.experience;
        }

        @Override
        public void setExperience(final double experience) {
            if (experience < 0) {
                throw new IllegalArgumentException("Experience must be positive!");
            }
            this.experience = experience;
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(final boolean cancel) {
            this.cancelled = cancel;
        }
    }

    static class Post extends Base implements ChangeExperienceEvent.Post {

        private final double experience;

        Post(final Cause cause, final Skill skill, final double originalExperience, final double experience) {
            super(cause, skill, originalExperience);
            this.experience = experience;
        }

        @Override
        public double experience() {
            return this.experience;
        }
    }

    static final class Level extends Post implements ChangeExperienceEvent.Post.Level {

        private final int originalLevel;
        private final int level;

        Level(final Cause cause, final Skill skill, final double originalExperience, final double experience, final int originalLevel,
                final int level) {
            super(cause, skill, originalExperience, experience);
            this.originalLevel = originalLevel;
            this.level = level;
        }

        @Override
        public int originalLevel() {
            return this.originalLevel;
        }

        @Override
        public int level() {
            return this.level;
        }
    }
}
