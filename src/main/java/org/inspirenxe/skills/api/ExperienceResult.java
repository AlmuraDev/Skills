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

import java.util.Objects;
import java.util.Optional;

/**
 * The result of changing the experience of a {@link Skill}.
 */
public final class ExperienceResult {

    public enum Type {
        SUCCESS,
        CANCELLED,
        ERROR
    }

    private static final ExperienceResult CANCELLED = new ExperienceResult(Type.CANCELLED, 0, null);

    private final Type type;
    private final double xp;
    private final Exception exception;

    private ExperienceResult(final Type type, final double xp, final Exception exception) {
        this.type = type;
        this.xp = xp;
        this.exception = exception;
    }

    public static ExperienceResult success(final double xp) {
        return new ExperienceResult(Type.SUCCESS, xp, null);
    }

    public static ExperienceResult cancelled() {
        return ExperienceResult.CANCELLED;
    }

    public static ExperienceResult error(final Exception exception) {
        return new ExperienceResult(Type.ERROR, 0, Objects.requireNonNull(exception, "exception"));
    }

    public Type type() {
        return this.type;
    }

    /**
     * @return The experience actually changed by (0 unless {@link Type#SUCCESS})
     */
    public double xp() {
        return this.xp;
    }

    public Optional<Exception> exception() {
        return Optional.ofNullable(this.exception);
    }
}
