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
package org.inspirenxe.skills.api;

/**
 * Entry point to the Skills API. SpongeAPI 21 only provides its own services, so the {@link SkillService} is reached through here.
 */
public final class Skills {

    public static final String ID = "skills";

    private static volatile SkillService service;

    private Skills() {
    }

    /**
     * @return The skill service
     * @throws IllegalStateException If Skills is not enabled (yet)
     */
    public static SkillService service() {
        final SkillService current = Skills.service;
        if (current == null) {
            throw new IllegalStateException("Skills is not enabled!");
        }
        return current;
    }

    /**
     * @return True once the skill service is available
     */
    public static boolean isAvailable() {
        return Skills.service != null;
    }

    /**
     * Internal, set by the Skills plugin.
     */
    public static void setService(final SkillService service) {
        Skills.service = service;
    }
}
