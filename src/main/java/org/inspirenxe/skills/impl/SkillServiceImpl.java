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

import com.google.inject.Inject;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.inspirenxe.skills.generated.tables.records.SkillsExperienceRecord;
import org.inspirenxe.skills.impl.database.DatabaseManager;
import org.inspirenxe.skills.impl.database.Queries;
import org.inspirenxe.skills.impl.event.experience.load.LoadExperiencePostEventImpl;
import org.inspirenxe.skills.impl.event.experience.load.LoadExperiencePreEventImpl;
import org.inspirenxe.skills.impl.event.experience.save.SaveExperiencePostEventImpl;
import org.inspirenxe.skills.impl.event.experience.save.SaveExperiencePreEventImpl;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public final class SkillServiceImpl implements SkillService {

  private final PluginContainer container;
  private final Scheduler scheduler;
  private final EventManager eventManager;
  private final GameRegistry registry;
  private final DatabaseManager databaseManager;
  private final SkillHolderImpl.Factory factory;

  private final Map<UUID, Set<SkillHolder>> containers = new HashMap<>();
  private final Map<UUID, Task> tasks = new HashMap<>();
  private final Map<UUID, SaveContainerToDatabase> runnables = new HashMap<>();

  @Inject
  public SkillServiceImpl(final PluginContainer container, final Scheduler scheduler, final EventManager eventManager,
    final GameRegistry registry, final DatabaseManager databaseManager, final SkillHolderImpl.Factory factory) {
    this.container = container;
    this.scheduler = scheduler;
    this.registry = registry;
    this.eventManager = eventManager;
    this.databaseManager = databaseManager;
    this.factory = factory;
  }

  @Override
  public DecimalFormat getXpFormat() {
    return new DecimalFormat("#,###.##");
  }

  @Override
  public Map<UUID, Set<SkillHolder>> getHolders() {
    return Collections.unmodifiableMap(this.containers);
  }

  @Override
  public void loadContainer(final UUID container) {
    Task task = this.tasks.remove(container);

    if (task != null) {
      task.cancel();
    }

    this.runnables.remove(container);

    final SaveContainerToDatabase runnable = new SaveContainerToDatabase(this, container);

    task = this.scheduler
      .createTaskBuilder()
      .async()
      .interval(30, TimeUnit.SECONDS)
      .execute(runnable)
      .submit(this.container);

    this.tasks.put(container, task);
    this.runnables.put(container, runnable);
  }

  @Override
  public void removeContainer(final UUID container) {
    checkNotNull(container);

    this.containers.remove(container);

    final Task scheduled = this.tasks.remove(container);

    if (scheduled != null) {
      scheduled.cancel();
    }
    this.runnables.remove(container);
  }

  @Override
  public void saveContainer(final UUID container, final boolean async) {
    checkNotNull(container);

    final Set<SkillHolder> holders = this.containers.get(container);

    if (holders == null || holders.isEmpty()) {
      return;
    }

    final SaveContainerToDatabase runnable = this.runnables.get(container);

    if (runnable == null) {
      return;
    }

    for (final SkillHolder holder : holders) {
      runnable.queue(holder, this.calculateDirtySkills(holder));
    }

    if (!async) {
      runnable.run();
    }
  }

  @Override
  public Set<SkillHolder> getHoldersInContainer(final UUID container) {
    checkNotNull(container);

    return Collections.unmodifiableSet(this.containers.computeIfAbsent(container, v -> new HashSet<>()));
  }

  @Override
  public Optional<SkillHolder> getHolder(final UUID container, final UUID holder) {
    final Set<SkillHolder> holders = this.containers.get(container);

    SkillHolder found = null;

    if (holders != null) {
      for (final SkillHolder skillHolder : holders) {
        if (skillHolder.getHolderUniqueId().equals(holder)) {
          found = skillHolder;
          break;
        }
      }
    }

    return Optional.ofNullable(found);
  }

  @Override
  public void loadHolder(final UUID container, final UUID holder, final boolean async) {
    final SkillHolder skillHolder = this.factory.create(container, holder);

    // Load skills for the skillHolder in this new container
    this.containers.computeIfAbsent(container, k -> {
      this.loadContainer(container);
      return new HashSet<>();
    }).add(skillHolder);

    final Collection<SkillType> skillTypes = this.registry.getAllOf(SkillType.class);
    skillTypes.forEach(skillHolder::addSkill);

    this.scheduler
      .createTaskBuilder()
      .async()
      .execute(() -> this.loadSkills(skillHolder.getContainerUniqueId(), skillHolder.getHolderUniqueId(), skillTypes))
      .submit(this.container);
  }

  @Override
  public void removeHolder(final UUID container, final UUID holder) {
    checkNotNull(container);
    checkNotNull(holder);

    final Set<SkillHolder> skillHolders = this.containers.get(container);

    if (skillHolders != null) {

      final Iterator<SkillHolder> iter = skillHolders.iterator();

      while (iter.hasNext()) {

        final SkillHolder skillHolder = iter.next();

        if (skillHolder.getHolderUniqueId().equals(holder)) {

          iter.remove();

          final SaveContainerToDatabase runnable = this.runnables.get(container);
          if (runnable != null) {
            runnable.remove(skillHolder);
          }

          break;
        }
      }
    }
  }

  @Override
  public void saveHolder(final SkillHolder skillHolder, final boolean async) {

    if (!async) {
      final SaveContainerToDatabase runnable = new SaveContainerToDatabase(this, skillHolder.getContainerUniqueId());
      runnable.queue(skillHolder, this.calculateDirtySkills(skillHolder));
      runnable.run();
      return;
    }

    final SaveContainerToDatabase runnable = this.runnables.get(skillHolder.getContainerUniqueId());

    if (runnable == null) {
      return;
    }

    runnable.queue(skillHolder, this.calculateDirtySkills(skillHolder));
  }

  private Map<SkillType, Double> calculateDirtySkills(final SkillHolder skillHolder) {
    final Map<SkillType, Double> skills = new HashMap<>();

    for (final Map.Entry<SkillType, Skill> entry : skillHolder.getSkills().entrySet()) {
      final SkillType skillType = entry.getKey();
      final Skill skill = entry.getValue();

      if (skill.isInitialized() && skill.isDirty()) {
        skills.put(skillType, skill.getCurrentExperience());
      }
    }

    return skills;
  }

  private void loadSkills(final UUID container, final UUID holder, final Collection<SkillType> skillTypes) {

    // Commence loading of skills
    try (final DSLContext context = this.databaseManager.createContext(true)) {
      for (final SkillType skillType : skillTypes) {

        final SkillsExperienceRecord record = Queries
          .createFetchExperienceQuery(container, holder, skillType.getId())
          .build(context)
          .keepStatement(false)
          .fetchOne();

        final double dbExperience;
        final boolean isNewSkill;

        if (record == null) {
          dbExperience = 0;
          isNewSkill = true;
        } else {
          dbExperience = record.getExperience().doubleValue();
          isNewSkill = false;
        }

        final ExperienceEvent.Load.Pre event;

        if (!Sponge.getServer().isMainThread()) {
          event = new LoadExperiencePreEventImpl(Cause.of(EventContext.empty(), this), container, holder, skillType, dbExperience, dbExperience,
            !isNewSkill);

          this.eventManager.post(event);
        } else {
          try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            event = new LoadExperiencePreEventImpl(frame.getCurrentCause(), container, holder, skillType, dbExperience, dbExperience,
              !isNewSkill);

            this.eventManager.post(event);
          }
        }

        final double experience = dbExperience;

        this.scheduler
          .createTaskBuilder()
          .execute(() -> {
            final SkillHolder applyHolder = this.getHolder(container, holder).orElse
              (null);

            if (applyHolder == null) {
              return;
            }

            final Skill skill = applyHolder.getSkill(skillType).orElse(null);

            if (skill != null) {

              try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(SkillServiceImpl.this);

                skill.setExperience(experience);

                this.eventManager.post(new LoadExperiencePostEventImpl(frame.getCurrentCause(), skill, dbExperience, skill.getCurrentExperience(),
                  !isNewSkill));
              }
            }
          })
          .submit(this.container);
      }
    } catch (final SQLException e) {
      e.printStackTrace();
    }
  }

  private void saveSkills(final UUID container, final UUID holder, final Map<SkillType, Double> skillTypesByExperience) {

    final HashSet<Query> batchInsert = new HashSet<>();
    final HashSet<Query> batchUpdate = new HashSet<>();

    final HashSet<ExperienceEvent.Save.Pre> preEvents = new HashSet<>();

    try (final DSLContext context = this.databaseManager.createContext(true)) {
      for (final Map.Entry<SkillType, Double> entry : skillTypesByExperience.entrySet()) {
        final SkillType skillType = entry.getKey();

        final double skillExperience = entry.getValue();

        final ExperienceEvent.Save.Pre event;

        if (!Sponge.getServer().isMainThread()) {
          event = new SaveExperiencePreEventImpl(Cause.of(EventContext.empty(), this), container, holder, skillType, skillExperience,
            skillExperience);

          this.eventManager.post(event);
        } else {
          try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            event = new SaveExperiencePreEventImpl(frame.getCurrentCause(), container, holder, skillType, skillExperience,
              skillExperience);

            this.eventManager.post(event);
          }
        }

        this.eventManager.post(event);

        final int result = Queries
          .createHasExperienceInSkillQuery(container, holder, skillType.getId())
          .build(context)
          .keepStatement(false)
          .execute();

        if (result == 0) {
          batchInsert.add(Queries
            .createInsertSkillExperienceQuery(container, holder, skillType.getId(), event.getExperience())
            .build(context)
            .keepStatement(false));
        } else {
          batchUpdate.add(Queries
            .createUpdateSkillExperienceQuery(container, holder, skillType.getId(), event.getExperience(), Timestamp.from(Instant.now()))
            .build(context)
            .keepStatement(false));
        }


        preEvents.add(event);
      }

      context.batch(batchInsert).execute();

      context.batch(batchUpdate).execute();

      final Runnable runnable = () -> {
        for (ExperienceEvent.Save.Pre preEvent : preEvents) {
          final SkillHolder skillHolder = this.getHolder(preEvent.getContainerUniqueId(), preEvent
            .getHolderUniqueId()).orElse(null);

          if (skillHolder == null) {
            continue;
          }

          final Skill skill = skillHolder.getSkill(preEvent.getSkillType()).orElse(null);

          if (skill == null) {
            continue;
          }

          try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);

            this.eventManager.post(new SaveExperiencePostEventImpl(frame.getCurrentCause(), skill, preEvent.getOriginalExperience(),
              preEvent.getExperience()));
          }

          skill.setDirty(false);
        }
      };

      if (!Sponge.getServer().isMainThread()) {
        this.scheduler
          .createTaskBuilder()
          .execute(runnable)
          .submit(this.container);
      } else {
        runnable.run();
      }
    } catch (final SQLException e) {
      e.printStackTrace();
    }
  }

  private static final class SaveContainerToDatabase implements Runnable {

    private final SkillServiceImpl skillManager;
    private final UUID container;
    private final Queue<DirtySkillHolderQueueEntry> queued = new ConcurrentLinkedQueue<>();

    private boolean skip = false;

    SaveContainerToDatabase(final SkillServiceImpl skillManager, final UUID container) {
      this.skillManager = skillManager;
      this.container = container;
    }

    @Override
    public void run() {

      if (this.skip) {
        return;
      }

      DirtySkillHolderQueueEntry entry;

      while ((entry = this.queued.poll()) != null) {
        if (this.skip) {
          break;
        }

        this.skillManager.saveSkills(this.container, entry.holder, entry.dirtySkills);
      }
    }

    public void queue(final SkillHolder skillHolder, final Map<SkillType, Double> dirtySkills) {
      this.queued.add(new DirtySkillHolderQueueEntry(skillHolder.getHolderUniqueId(), dirtySkills));
    }

    public void remove(final SkillHolder skillHolder) {
      this.skip = true;
      this.queued.removeIf(queueable -> queueable.equals(new DirtySkillHolderQueueEntry(skillHolder.getHolderUniqueId(), new HashMap<>())));
      this.skip = false;
    }
  }

  private static final class DirtySkillHolderQueueEntry {

    final UUID holder;
    final Map<SkillType, Double> dirtySkills = new HashMap<>();

    private DirtySkillHolderQueueEntry(final UUID holder, final Map<SkillType, Double> dirtySkills) {
      this.holder = holder;
      this.dirtySkills.putAll(dirtySkills);
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof DirtySkillHolderQueueEntry)) {
        return false;
      }
      final DirtySkillHolderQueueEntry entry = (DirtySkillHolderQueueEntry) o;
      return Objects.equals(this.holder, entry.holder);
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.holder);
    }
  }

  public interface Factory {
    SkillServiceImpl create();
  }
}
