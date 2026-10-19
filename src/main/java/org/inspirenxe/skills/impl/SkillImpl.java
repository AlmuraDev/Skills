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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.inspirenxe.skills.api.event.ChangeExperienceEvent;
import org.inspirenxe.skills.api.event.LoadExperienceEvent;
import org.inspirenxe.skills.api.event.SaveExperienceEvent;
import org.inspirenxe.skills.api.event.SkillsEventFactory;
import org.inspirenxe.skills.api.result.Result;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.result.experience.ExperienceResult;
import org.inspirenxe.skills.generated.tables.records.SkillsExperienceRecord;
import org.inspirenxe.skills.impl.database.DatabaseManager;
import org.inspirenxe.skills.impl.database.Queries;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

public final class SkillImpl implements Skill {

  private final PluginContainer container;
  private final Scheduler scheduler;
  private final EventManager eventManager;
  private final DatabaseManager databaseManager;
  private final Logger logger;
  private final SkillHolder holder;
  private final SkillType type;
  private volatile double experience;
  private volatile boolean isDirty = false;
  // Experience may only be changed and saved once successfully loaded from the database
  private volatile boolean loaded = false;
  private volatile boolean loadFailed = false;
  // Experience added before the load completed (main thread only), applied on top of the loaded value
  private double pendingExperience;

  @Inject
  private SkillImpl(final PluginContainer container, final Scheduler scheduler, final EventManager eventManager, final DatabaseManager
    databaseManager, final Logger logger, @Assisted final SkillHolder holder, @Assisted final SkillType type) {
    checkNotNull(type);
    checkNotNull(holder);

    this.container = container;
    this.scheduler = scheduler;
    this.eventManager = eventManager;
    this.databaseManager = databaseManager;
    this.logger = logger;
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

  /**
   * @return True once the experience has been loaded from the database (levels are only accurate after that)
   */
  public boolean isLoaded() {
    return this.loaded;
  }

  @Override
  public ExperienceResult setExperience(final double experience) {
    Exception exception = null;

    if (!this.loaded) {
      exception = new IllegalStateException(this.loadFailed ? "Experience failed to load from the database!" : "Experience is not loaded yet!");
    } else if (experience < 0) {
      exception = new ArithmeticException("Experience must be positive!");
    }

    if (exception != null) {
      return ExperienceResult.builder().type(Result.Type.ERROR).exception(exception).build();
    }

    final int originalLevel = this.getCurrentLevel();
    final double originalExperience = this.experience;

    final ChangeExperienceEvent.Pre event = SkillsEventFactory.createChangeExperienceEventPre(Sponge.getCauseStackManager().getCurrentCause(),
      this, originalExperience, experience);

    if (this.eventManager.post(event)) {
      return ExperienceResult.builder().type(Result.Type.CANCELLED).build();
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
    return ExperienceResult.builder().type(Result.Type.SUCCESS).xp(event.getExperienceDifference()).build();
  }

  @Override
  public ExperienceResult addExperience(final double experience) {
    if (!this.loaded) {
      if (this.loadFailed) {
        return ExperienceResult.builder().type(Result.Type.ERROR).exception(new IllegalStateException("Experience failed to load from the "
          + "database!")).build();
      }

      this.pendingExperience += experience;
      return ExperienceResult.builder().type(Result.Type.SUCCESS).xp(experience).build();
    }

    return this.setExperience(this.getCurrentExperience() + experience);
  }

  @Override
  public void load() {
    final UUID containerId = this.holder.getContainer().getUniqueId();
    final UUID holderId = this.holder.getUniqueId();

    this.databaseManager.execute(() -> {
      SkillsExperienceRecord record = null;
      boolean success = true;

      try (final DSLContext context = this.databaseManager.createContext(true)) {
        record = Queries
          .createFetchExperienceQuery(containerId, holderId, this.type.getId())
          .build(context)
          .keepStatement(false)
          .fetchOne();
      } catch (final Exception ex) {
        success = false;
        this.logger.error("Failed to load experience for holder '{}' ({}) in skill '{}' for container '{}'! Experience changes and saves are "
          + "disabled for this skill until it loads successfully.", this.holder.getName(), holderId, this.type.getId(), containerId, ex);
      }

      final boolean finalSuccess = success;
      final SkillsExperienceRecord finalRecord = record;

      this.scheduler
        .createTaskBuilder()
        .execute(() -> {
          try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            this.completeLoad(finalSuccess, finalRecord);
          }
        })
        .submit(this.container);
    });
  }

  private void completeLoad(final boolean success, @Nullable final SkillsExperienceRecord record) {
    if (!success) {
      if (!this.loaded) {
        this.loadFailed = true;
      }
      return;
    }

    final CauseStackManager causeStack = Sponge.getCauseStackManager();
    final UUID containerId = this.holder.getContainer().getUniqueId();
    final UUID holderId = this.holder.getUniqueId();

    final double originalExperience = this.experience;

    final double dbExperience = record == null ? 0 : record.getExperience().doubleValue();

    final boolean hasGainedExperienceBefore = originalExperience == 0 && record == null;

    final LoadExperienceEvent.Pre loadPreEvent = SkillsEventFactory.createLoadExperienceEventPre(causeStack.getCurrentCause(), containerId,
      holderId, this.type, originalExperience, dbExperience, hasGainedExperienceBefore);

    this.loadFailed = false;

    if (!this.eventManager.post(loadPreEvent)) {
      this.experience = loadPreEvent.getExperience();
      this.loaded = true;

      final LoadExperienceEvent.Post loadPostEvent = SkillsEventFactory.createLoadExperienceEventPost(causeStack.getCurrentCause(), this,
        originalExperience, this.experience, hasGainedExperienceBefore);

      this.eventManager.post(loadPostEvent);
    } else {
      this.loaded = true;
    }

    if (this.pendingExperience != 0) {
      final double pending = this.pendingExperience;
      this.pendingExperience = 0;
      this.addExperience(pending);
    }
  }

  @Override
  public void save() {
    if (!Sponge.getServer().isMainThread()) {
      // State is snapshotted on the main thread, only the database write happens asynchronously
      this.scheduler.createTaskBuilder().execute(this::save).submit(this.container);
      return;
    }

    if (!this.loaded) {
      if (this.isDirty || this.pendingExperience != 0) {
        this.logger.warn("Not saving experience for holder '{}' ({}) in skill '{}' as it was never loaded from the database.",
          this.holder.getName(), this.holder.getUniqueId(), this.type.getId());
      }
      return;
    }

    if (!this.isDirty) {
      return;
    }

    final CauseStackManager causeStack = Sponge.getCauseStackManager();
    final UUID containerId = this.holder.getContainer().getUniqueId();
    final UUID holderId = this.holder.getUniqueId();

    final double originalExperience = this.experience;

    final SaveExperienceEvent.Pre savePreEvent = SkillsEventFactory.createSaveExperienceEventPre(causeStack.getCurrentCause(), containerId,
      holderId, this.type, originalExperience, originalExperience);

    if (this.eventManager.post(savePreEvent)) {
      return;
    }

    final double experience = savePreEvent.getExperience();
    final AtomicBoolean success = new AtomicBoolean();

    final CompletableFuture<Void> future = this.databaseManager.execute(() -> success.set(this.write(containerId, holderId, experience)));

    if (this.databaseManager.isStopping()) {
      // Server is going down, wait for the write so it is not lost
      future.join();

      if (success.get()) {
        this.completeSave(originalExperience, experience);
      }
    } else {
      future.thenRun(() -> {
        if (!success.get()) {
          return;
        }

        this.scheduler
          .createTaskBuilder()
          .execute(() -> {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
              this.completeSave(originalExperience, experience);
            }
          })
          .submit(this.container);
      });
    }
  }

  private boolean write(final UUID containerId, final UUID holderId, final double experience) {
    try (final DSLContext context = this.databaseManager.createContext(true)) {
      final Record1<Integer> record = Queries
        .createHasExperienceInSkillQuery(containerId, holderId, this.type.getId())
        .build(context)
        .keepStatement(false)
        .fetchOne();

      final int result;

      if (record == null) {
        result = Queries
          .createInsertSkillExperienceQuery(containerId, holderId, this.type.getId(), experience)
          .build(context)
          .execute();
      } else {
        result = Queries
          .createUpdateSkillExperienceQuery(containerId, holderId, this.type.getId(), experience, Timestamp.from(Instant.now()))
          .build(context)
          .execute();
      }

      if (result == 0) {
        this.logger.error("Failed to save experience for holder '{}' ({}) in skill '{}' for container '{}': no rows affected.",
          this.holder.getName(), holderId, this.type.getId(), containerId);
        return false;
      }

      return true;
    } catch (final Exception ex) {
      this.logger.error("Failed to save experience for holder '{}' ({}) in skill '{}' for container '{}'!", this.holder.getName(), holderId,
        this.type.getId(), containerId, ex);
      return false;
    }
  }

  private void completeSave(final double originalExperience, final double savedExperience) {
    // Only clear dirty if nothing changed while saving, otherwise the newer experience still needs saving
    if (this.experience == originalExperience) {
      this.experience = savedExperience;
      this.isDirty = false;
    }

    final SaveExperienceEvent.Post savePostEvent = SkillsEventFactory.createSaveExperienceEventPost(Sponge.getCauseStackManager()
      .getCurrentCause(), this, originalExperience, savedExperience);

    this.eventManager.post(savePostEvent);
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
      .add("type", this.type)
      .toString();
  }

  public interface Factory {
    SkillImpl create(final SkillHolder holder, final SkillType type);
  }
}
