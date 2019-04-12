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
package org.inspirenxe.skills.impl.skill;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.inspirenxe.skills.api.event.ChangeExperienceEvent;
import org.inspirenxe.skills.api.event.LoadExperienceEvent;
import org.inspirenxe.skills.api.event.SaveExperienceEvent;
import org.inspirenxe.skills.api.event.SkillsEventFactory;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.SkillType;
import org.inspirenxe.skills.api.skill.holder.SkillHolder;
import org.inspirenxe.skills.generated.tables.records.SkillsExperienceRecord;
import org.inspirenxe.skills.impl.database.DatabaseManager;
import org.inspirenxe.skills.impl.database.Queries;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class SkillImpl implements Skill {

    private final PluginContainer container;
    private final Scheduler scheduler;
    private final EventManager eventManager;
    private final DatabaseManager databaseManager;
    private final SkillHolder holder;
    private final SkillType type;
    private double experience;
    private boolean isDirty = false;

    @Inject
    private SkillImpl(final PluginContainer container, final Scheduler scheduler, final EventManager eventManager, final DatabaseManager
        databaseManager, @Assisted final SkillHolder holder, @Assisted final SkillType type) {
        checkNotNull(type);
        checkNotNull(holder);

        this.container = container;
        this.scheduler = scheduler;
        this.eventManager = eventManager;
        this.databaseManager = databaseManager;
        this.type = type;
        this.holder = holder;
    }

    @Override
    public SkillHolder getHolder() {
        return this.holder;
    }

    @Override
    public SkillType getSkillType() {
        return this.type;
    }

    @Override
    public String getName() {
        return this.type.getName();
    }

    @Override
    public double getCurrentExperience() {
        return this.experience;
    }

    @Override
    public void setExperience(final double experience) {
        checkState(experience >= 0, "Experience must be positive!");

        final int originalLevel = this.getCurrentLevel();
        final double originalExperience = this.experience;

        final ChangeExperienceEvent.Pre event = SkillsEventFactory.createChangeExperienceEventPre(Sponge.getCauseStackManager().getCurrentCause(),
            this, originalExperience, experience);

        if (this.eventManager.post(event)) {
            return;
        }

        this.experience = event.getExperience();

        final int level = this.getCurrentLevel();

        final CauseStackManager causeStack = Sponge.getCauseStackManager();

        if (originalLevel != level) {
            this.eventManager.post(SkillsEventFactory.createChangeExperienceEventPostLevel(causeStack.getCurrentCause(), this,
                originalExperience, this.experience, originalLevel, level));
        } else {
            this.eventManager.post(SkillsEventFactory.createChangeExperienceEventPost(causeStack.getCurrentCause(), this,
                originalExperience, this.experience));
        }

        this.isDirty = true;
    }

    @Override
    public void addExperience(final double experience) {
        this.setExperience(this.getCurrentExperience() + experience);
    }

    @Override
    public void load() {
        final CauseStackManager causeStack = Sponge.getCauseStackManager();
        final UUID containerId = this.holder.getContainer().getUniqueId();
        final UUID holderId = this.holder.getUniqueId();

        SkillsExperienceRecord record = null;

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            record = Queries
                .createFetchExperienceQuery(containerId, holderId, this.type.getId())
                .build(context)
                .keepStatement(false)
                .fetchOne();

        } catch (final SQLException ex) {
            ex.printStackTrace();
        }

        final double originalExperience = this.experience;

        final double dbExperience = record == null ? 0 : record.getExperience().doubleValue();

        final boolean hasGainedExperienceBefore = originalExperience == 0 && record == null;

        final LoadExperienceEvent.Pre loadPreEvent;

        if (!Sponge.getServer().isMainThread()) {
            loadPreEvent = SkillsEventFactory.createLoadExperienceEventPre(Cause.of(EventContext.empty(), this), containerId, holderId, this.type,
                originalExperience, dbExperience, hasGainedExperienceBefore);
        } else {
            loadPreEvent = SkillsEventFactory.createLoadExperienceEventPre(causeStack.getCurrentCause(), containerId, holderId, this.type,
                originalExperience, dbExperience, hasGainedExperienceBefore);
        }

        if (this.eventManager.post(loadPreEvent)) {
            return;
        }

        final double postExperience = loadPreEvent.getExperience();

        final Runnable runnable = () -> {
            SkillImpl.this.experience = postExperience;

            final LoadExperienceEvent.Post loadPostEvent = SkillsEventFactory.createLoadExperienceEventPost(causeStack.getCurrentCause(),
                SkillImpl.this, originalExperience, SkillImpl.this.experience, hasGainedExperienceBefore);

            SkillImpl.this.eventManager.post(loadPostEvent);
        };

        if (!Sponge.getServer().isMainThread()) {
            this.scheduler
                .createTaskBuilder()
                .execute(() -> {
                    try (final CauseStackManager.StackFrame frame = causeStack.pushCauseFrame()) {
                        runnable.run();
                    }
                })
                .submit(this.container);
        } else {
            runnable.run();
        }
    }

    @Override
    public void save() {
        if (!this.isDirty) {
            return;
        }

        final CauseStackManager causeStack = Sponge.getCauseStackManager();
        final UUID containerId = this.holder.getContainer().getUniqueId();
        final UUID holderId = this.holder.getUniqueId();

        final double originalExperience = this.experience;

        final SaveExperienceEvent.Pre savePreEvent;

        if (!Sponge.getServer().isMainThread()) {
            savePreEvent = SkillsEventFactory.createSaveExperienceEventPre(Cause.of(EventContext.empty(), this), containerId, holderId,
                this.type, originalExperience, originalExperience);
        } else {
            savePreEvent = SkillsEventFactory.createSaveExperienceEventPre(causeStack.getCurrentCause(), containerId, holderId,
                this.type, originalExperience, originalExperience);
        }

        if (this.eventManager.post(savePreEvent)) {
            return;
        }

        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final Record1<Integer> record = Queries
                .createHasExperienceInSkillQuery(this.holder.getContainer().getUniqueId(), this.holder.getUniqueId(), this.type.getId())
                .build(context)
                .keepStatement(false)
                .fetchOne();

            boolean success = true;

            if (record == null) {
                final int result = Queries
                    .createInsertSkillExperienceQuery(containerId, holderId, this.type.getId(), savePreEvent.getExperience())
                    .build(context)
                    .execute();

                if (result == 0) {
                    // TODO Database insert failed, what do?
                    success = false;
                }
            } else {
                final int result = Queries
                    .createUpdateSkillExperienceQuery(containerId, holderId, this.type.getId(), savePreEvent.getExperience(),
                        Timestamp.from(Instant.now()))
                    .build(context)
                    .execute();

                if (result == 0) {
                    success = false;
                }
            }

            if (!success) {
                return;
            }

            final Runnable runnable = () -> {
                SkillImpl.this.experience = savePreEvent.getExperience();

                final SaveExperienceEvent.Post savePostEvent =
                    SkillsEventFactory
                        .createSaveExperienceEventPost(causeStack.getCurrentCause(), SkillImpl.this, originalExperience, SkillImpl.this.experience);

                this.eventManager.post(savePostEvent);

                SkillImpl.this.isDirty = false;
            };

            if (!Sponge.getServer().isMainThread()) {
                this.scheduler
                    .createTaskBuilder()
                    .execute(() -> {
                        try (final CauseStackManager.StackFrame frame = causeStack.pushCauseFrame()) {
                            runnable.run();
                        }
                    })
                    .submit(this.container);
            } else {
                runnable.run();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SkillImpl)) {
            return false;
        }
        final SkillImpl that = (SkillImpl) o;
        return Objects.equals(this.holder, that.holder) && Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.holder, this.type);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("container", this.holder.getContainer().getUniqueId())
            .add("holder", this.holder.getUniqueId())
            .add("skill", this.type)
            .toString();
    }

    public interface Factory {

        SkillImpl create(final SkillHolder holder, final SkillType type);
    }
}
