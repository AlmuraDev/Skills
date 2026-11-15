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

import org.apache.logging.log4j.Logger;
import org.inspirenxe.skills.Tasks;
import org.inspirenxe.skills.api.ExperienceResult;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.database.Database;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;

import java.util.OptionalDouble;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * One skill of one holder. Experience lives on the server thread; the database is only read and written on the database thread.
 */
public final class SkillImpl implements Skill {

    private final Logger logger;
    private final Database database;
    private final SkillHolder holder;
    private final SkillType type;

    private volatile double experience;
    private volatile boolean dirty;
    // Experience may only be changed and saved once it loaded from the database
    private volatile boolean loaded;
    private volatile boolean loadFailed;
    // Experience added before the load completed (server thread only), applied on top of the loaded value
    private double pendingExperience;

    SkillImpl(final Logger logger, final Database database, final SkillHolder holder, final SkillType type) {
        this.logger = logger;
        this.database = database;
        this.holder = holder;
        this.type = type;
    }

    @Override
    public SkillType type() {
        return this.type;
    }

    @Override
    public SkillHolder holder() {
        return this.holder;
    }

    @Override
    public double experience() {
        return this.experience;
    }

    @Override
    public boolean isLoaded() {
        return this.loaded;
    }

    @Override
    public ExperienceResult setExperience(final double experience) {
        if (!this.loaded) {
            return ExperienceResult.error(new IllegalStateException(this.loadFailed ? "Experience failed to load from the database!"
                    : "Experience is not loaded yet!"));
        }
        if (experience < 0) {
            return ExperienceResult.error(new ArithmeticException("Experience must be positive!"));
        }
        final CauseStackManager causes = Sponge.server().causeStackManager();
        final int originalLevel = this.level();
        final double originalExperience = this.experience;
        final ExperienceEvents.Pre pre = new ExperienceEvents.Pre(causes.currentCause(), this, originalExperience, experience);
        if (Sponge.eventManager().post(pre)) {
            return ExperienceResult.cancelled();
        }
        this.experience = pre.experience();
        this.dirty = true;
        final int level = this.level();
        if (originalLevel != level) {
            Sponge.eventManager().post(new ExperienceEvents.Level(causes.currentCause(), this, originalExperience, this.experience,
                    originalLevel, level));
        } else {
            Sponge.eventManager().post(new ExperienceEvents.Post(causes.currentCause(), this, originalExperience, this.experience));
        }
        return ExperienceResult.success(this.experience - originalExperience);
    }

    @Override
    public ExperienceResult addExperience(final double experience) {
        if (!this.loaded) {
            if (this.loadFailed) {
                return ExperienceResult.error(new IllegalStateException("Experience failed to load from the database!"));
            }
            this.pendingExperience += experience;
            return ExperienceResult.success(experience);
        }
        return this.setExperience(Math.max(0, this.experience + experience));
    }

    @Override
    public void load() {
        final String container = this.holder.container().key().asString();
        final UUID holderId = this.holder.uniqueId();
        this.database.execute(() -> {
            OptionalDouble stored = OptionalDouble.empty();
            boolean success = true;
            try {
                stored = this.database.fetch(container, holderId, this.type.key().asString());
            } catch (final Exception e) {
                success = false;
                this.logger.error("Failed to load the experience of '{}' ({}) in skill '{}' for container '{}'! Experience changes and saves are "
                        + "disabled for this skill until it loads.", this.holder.name(), holderId, this.type.key(), container, e);
            }
            final boolean loadedOk = success;
            final double value = stored.orElse(0);
            Tasks.sync(() -> this.completeLoad(loadedOk, value));
        }).exceptionally(e -> {
            this.logger.error("Could not queue loading the experience of '{}' in skill '{}'!", this.holder.name(), this.type.key(), e);
            return null;
        });
    }

    private void completeLoad(final boolean success, final double stored) {
        if (!success) {
            if (!this.loaded) {
                this.loadFailed = true;
            }
            return;
        }
        this.loadFailed = false;
        this.experience = stored;
        this.loaded = true;
        if (this.pendingExperience != 0) {
            final double pending = this.pendingExperience;
            this.pendingExperience = 0;
            try (CauseStackManager.StackFrame frame = Sponge.server().causeStackManager().pushCauseFrame()) {
                this.addExperience(pending);
            }
        }
    }

    @Override
    public void save() {
        if (!Sponge.server().onMainThread()) {
            // State is snapshotted on the server thread, only the database write happens on the database thread
            Tasks.sync(this::save);
            return;
        }
        if (!this.loaded) {
            if (this.dirty || this.pendingExperience != 0) {
                this.logger.warn("Not saving the experience of '{}' ({}) in skill '{}' as it never loaded from the database.",
                        this.holder.name(), this.holder.uniqueId(), this.type.key());
            }
            return;
        }
        if (!this.dirty) {
            return;
        }
        final String container = this.holder.container().key().asString();
        final UUID holderId = this.holder.uniqueId();
        final double experience = this.experience;
        final AtomicBoolean success = new AtomicBoolean();
        final CompletableFuture<Void> future = this.database.execute(() -> {
            try {
                this.database.store(container, holderId, this.type.key().asString(), experience);
                success.set(true);
            } catch (final Exception e) {
                this.logger.error("Failed to save the experience of '{}' ({}) in skill '{}' for container '{}'!", this.holder.name(), holderId,
                        this.type.key(), container, e);
            }
        });
        if (this.database.isStopping()) {
            // The server is going down, wait for the write so it is not lost
            try {
                future.join();
            } catch (final RuntimeException e) {
                this.logger.error("Failed to save the experience of '{}' in skill '{}' while stopping!", this.holder.name(), this.type.key(), e);
            }
            if (success.get()) {
                this.completeSave(experience);
            }
            return;
        }
        future.whenComplete((ignored, error) -> {
            if (error != null) {
                this.logger.error("Could not queue saving the experience of '{}' in skill '{}'!", this.holder.name(), this.type.key(), error);
            } else if (success.get()) {
                Tasks.sync(() -> this.completeSave(experience));
            }
        });
    }

    private void completeSave(final double savedExperience) {
        // Only clear dirty if nothing changed while saving, otherwise the newer experience still needs saving
        if (this.experience == savedExperience) {
            this.dirty = false;
        }
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof SkillImpl other && this.holder.equals(other.holder) && this.type.equals(other.type);
    }

    @Override
    public int hashCode() {
        return 31 * this.holder.hashCode() + this.type.hashCode();
    }

    @Override
    public String toString() {
        return "Skill{" + this.type.key() + ", holder=" + this.holder.uniqueId() + ", container=" + this.holder.container().key() + "}";
    }
}
