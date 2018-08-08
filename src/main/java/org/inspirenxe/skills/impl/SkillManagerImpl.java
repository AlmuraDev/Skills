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
import static org.inspirenxe.skills.impl.database.Queries.createFetchExperienceQuery;
import static org.inspirenxe.skills.impl.database.Queries.createHasExperienceInSkillQuery;
import static org.inspirenxe.skills.impl.database.Queries.createInsertSkillExperienceQuery;
import static org.inspirenxe.skills.impl.database.Queries.createUpdateSkillExperienceQuery;

import com.almuradev.toolbox.inject.event.Witness;
import com.almuradev.toolbox.inject.event.WitnessScope;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillManager;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.inspirenxe.skills.generated.Tables;
import org.inspirenxe.skills.impl.database.DatabaseManager;
import org.inspirenxe.skills.impl.event.experience.load.LoadExperiencePostEventImpl;
import org.inspirenxe.skills.impl.event.experience.load.LoadExperiencePreEventImpl;
import org.inspirenxe.skills.impl.event.experience.save.SaveExperiencePostEventImpl;
import org.inspirenxe.skills.impl.event.experience.save.SaveExperiencePreEventImpl;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
@WitnessScope.Sponge
public final class SkillManagerImpl implements SkillManager, Witness {

  private final PluginContainer container;
  private final DatabaseManager databaseManager;
  private final SpongeExecutorService executorService;
  private final SkillHolderImpl.Factory factory;

  private final Map<UUID, Set<SkillHolder>> containerHolders = new HashMap<>();
  private final Map<UUID, Task> savingTasks = new HashMap<>();
  private final Map<UUID, SaveContainerToDatabase> containerRunnables = new HashMap<>();

  public static SkillManagerImpl INSTANCE;

  @Inject
  public SkillManagerImpl(final PluginContainer container, final DatabaseManager databaseManager, final SpongeExecutorService executorService,
    final SkillHolderImpl.Factory factory) {
    INSTANCE = this;
    this.container = container;
    this.databaseManager = databaseManager;
    this.executorService = executorService;
    this.factory = factory;
  }

  @Override
  public Set<SkillHolder> getHoldersInContainer(final UUID container) {
    checkNotNull(container);

    Set<SkillHolder> holders = this.containerHolders.get(container);
    if (holders == null) {
      holders = new HashSet<>();
    }

    return Collections.unmodifiableSet(holders);
  }

  @Override
  public Optional<SkillHolder> getHolder(final UUID container, final UUID holder) {
    final Set<SkillHolder> holders = this.containerHolders.get(container);

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

  @Listener
  public void onUnloadWorld(final UnloadWorldEvent event) {
    this.unloadContainer(event.getTargetWorld().getUniqueId());
  }

  @Listener(order = Order.LAST)
  public void onClientConnectionJoinByPlayer(final ClientConnectionEvent.Join event, final @Root Player player) {
    final UUID container = player.getWorld().getUniqueId();
    final UUID holder = player.getUniqueId();

    final SkillHolder skillHolder = this.factory.create(container, holder);

    // Load skills for the skillHolder in this new container
    this.containerHolders.computeIfAbsent(container, k -> {
      this.loadContainer(container);
      return new HashSet<>();
    }).add(skillHolder);

    final Collection<SkillType> skillTypes = Sponge.getRegistry().getAllOf(SkillType.class);
    skillTypes.forEach(skillHolder::addSkill);

    Sponge.getScheduler().createTaskBuilder()
        .name(this.container.getName() + "- Load Skills [" + container + " | " + skillHolder + "]")
        .async()
        .execute(() -> this.loadSkillsAsync(skillHolder.getContainerUniqueId(), skillHolder.getHolderUniqueId(), Sponge.getRegistry().getAllOf
            (SkillType.class).stream().map(CatalogType::getId).collect(Collectors.toSet())))
        .submit(this.container);
  }

  @Listener
  public void onClientConnectionDisconnectByPlayer(final ClientConnectionEvent.Disconnect event, final @Root Player player) {
    final UUID container = player.getWorld().getUniqueId();
    final UUID holder = player.getUniqueId();

    // Save skills for the holder in the old container and remove them
    final Set<SkillHolder> holders = this.containerHolders.get(container);
    if (holders != null) {
      final SkillHolder previousHolder = holders.stream().filter((h) -> h.getHolderUniqueId().equals(holder)).findFirst()
          .orElse(null);

      if (previousHolder != null) {
        holders.remove(previousHolder);

        // Remove container map if we no longer have holders
        if (holders.isEmpty()) {
          this.unloadContainer(container);
        }

        final Map<String, Double> dirtySkills = new HashMap<>();

        for (final Map.Entry<SkillType, Skill> skillEntry : previousHolder.getSkills().entrySet()) {
          final SkillType skillType = skillEntry.getKey();
          final Skill skill = skillEntry.getValue();

          if (skill.isInitialized() && skill.isDirtyState()) {
            dirtySkills.put(skillType.getId(), skill.getCurrentExperience());
          }
        }

        if (!dirtySkills.isEmpty()) {
          Sponge.getScheduler().createTaskBuilder()
              .name(this.container.getName() + "- Save Skills [" + container + " | " + holder + "]")
              .async()
              .execute(new SaveHolderToDatabase(this, container, new DirtySkillHolderQueueEntry(holder, dirtySkills)))
              .submit(this.container);
        }
      }
    }
  }

  @Listener(order = Order.LAST)
  public void onMoveTeleportByPlayer(final MoveEntityEvent.Teleport event, final @Getter("getTargetEntity") Player player) {
    final UUID previousContainer = event.getFromTransform().getExtent().getUniqueId();
    final UUID holder = event.getTargetEntity().getUniqueId();
    final UUID container = event.getToTransform().getExtent().getUniqueId();

    // Detect world change, if not bail
    if (previousContainer.equals(container)) {
      return;
    }

    // Save skills for the skillHolder in the old container and remove them
    final Set<SkillHolder> holdersInContainer = this.containerHolders.get(previousContainer);
    if (holdersInContainer != null) {
      final SkillHolder previousHolder = holdersInContainer.stream().filter((h) -> h.getHolderUniqueId().equals(holder)).findFirst()
          .orElse(null);

      if (previousHolder != null) {
        holdersInContainer.remove(previousHolder);

        // Remove container map if we no longer have holders
        if (holdersInContainer.isEmpty()) {
          this.unloadContainer(previousContainer);
        }

        final Map<String, Double> dirtySkills = new HashMap<>();

        for (final Map.Entry<SkillType, Skill> skillEntry : previousHolder.getSkills().entrySet()) {
          final SkillType skillType = skillEntry.getKey();
          final Skill skill = skillEntry.getValue();

          if (skill.isInitialized() && skill.isDirtyState()) {
            dirtySkills.put(skillType.getId(), skill.getCurrentExperience());
          }
        }

        if (!dirtySkills.isEmpty()) {
          Sponge.getScheduler().createTaskBuilder()
              .name(this.container.getName() + "- Save Skills [" + previousContainer + " | " + holder + "]")
              .async()
              .execute(new SaveHolderToDatabase(this, previousContainer, new DirtySkillHolderQueueEntry(holder,
                  dirtySkills)))
              .submit(this.container);
        }
      }
    }

    final SkillHolder skillHolder = this.factory.create(container, holder);

    // Load skills for the skillHolder in this new container
    // Load skills for the skillHolder in this new container
    this.containerHolders.computeIfAbsent(container, k -> {
      this.loadContainer(container);
      return new HashSet<>();
    }).add(skillHolder);

    final Collection<SkillType> skillTypes = Sponge.getRegistry().getAllOf(SkillType.class);
    skillTypes.forEach(skillHolder::addSkill);

    Sponge.getScheduler().createTaskBuilder()
        .name(this.container.getName() + "- Load Skills [" + container + " | " + skillHolder + "]")
        .async()
        .execute(() -> this.loadSkillsAsync(skillHolder.getContainerUniqueId(), skillHolder.getHolderUniqueId(), skillTypes.stream().map(
            CatalogType::getId).collect(Collectors.toSet())))
        .submit(this.container);
  }

  private void loadSkillsAsync(final UUID container, final UUID holder, final Collection<String> skillTypeIds) {

    // Commence loading of skills
    try (final DSLContext context = this.databaseManager.createContext(true)) {
      for (final String skillTypeId : skillTypeIds) {
        createFetchExperienceQuery(container, holder, skillTypeId)
            .build(context)
            .keepStatement(false)
            .fetchAsync(this.executorService)
            .whenCompleteAsync((rows, ex) -> {
              final SkillType skillType = Sponge.getRegistry().getType(SkillType.class, skillTypeId).orElse(null);

              if (skillType == null) {
                return;
              }

              final double dbExperience;
              final boolean isNewSkill;

              if (rows.isEmpty()) {
                dbExperience = 0;
                isNewSkill = true;
              } else {
                dbExperience = rows.getValue(0, Tables.SKILLS_EXPERIENCE.EXPERIENCE).doubleValue();
                isNewSkill = false;
              }

              final ExperienceEvent.Load.Pre event = new LoadExperiencePreEventImpl(container, holder, skillType, dbExperience,
                  dbExperience, !isNewSkill);
              Sponge.getEventManager().post(event);

              final double experience = dbExperience;

              Sponge.getScheduler().createTaskBuilder()
                  .name(this.container.getName() + " - Apply Skill " + skillType.getName() + " Experience For [" + container +
                      " | " + holder + "]")
                  .execute(() -> {
                    final SkillHolder applyHolder = SkillManagerImpl.this.getHolder(container, holder).orElse
                        (null);

                    if (applyHolder == null) {
                      return;
                    }

                    final Skill skill = applyHolder.getSkill(skillType).orElse(null);

                    if (skill != null) {

                      Sponge.getCauseStackManager().pushCause(SkillManagerImpl.this);

                      skill.setExperience(experience);

                      Sponge.getCauseStackManager().popCause();

                      Sponge.getCauseStackManager().pushCause(SkillManagerImpl.this);
                      Sponge.getEventManager().post(new LoadExperiencePostEventImpl(skill, dbExperience, skill
                          .getCurrentExperience(), !isNewSkill));
                      Sponge.getCauseStackManager().popCause();
                    }
                  })
                  .submit(this.container);
            });
      }
    } catch (final SQLException e) {
      e.printStackTrace();
    }
  }

  private void saveSkillsAsync(final UUID container, final UUID holder, final Map<String, Double> skillTypesByExperience) {

    final HashSet<Query> batchInsert = new HashSet<>();
    final HashSet<Query> batchUpdate = new HashSet<>();

    final HashSet<ExperienceEvent.Save.Pre> preEvents = new HashSet<>();

    try (final DSLContext context = this.databaseManager.createContext(true)) {
      for (final Map.Entry<String, Double> skillEntry : skillTypesByExperience.entrySet()) {
        final String skillTypeId = skillEntry.getKey();
        final SkillType skillType = Sponge.getRegistry().getType(SkillType.class, skillTypeId).orElse(null);

        if (skillType == null) {
          continue;
        }

        final double skillExperience = skillEntry.getValue();

        final ExperienceEvent.Save.Pre event = new SaveExperiencePreEventImpl(container, holder, skillType, skillExperience,
            skillExperience);
        Sponge.getEventManager().post(event);

        final SelectConditionStep<Record1<Integer>> hasExperienceInSkillQuery = createHasExperienceInSkillQuery(container,
            holder, skillTypeId).build(context);

        final int result = hasExperienceInSkillQuery
            .keepStatement(false)
            .execute();

        if (result == 1) {
          batchInsert.add(createUpdateSkillExperienceQuery(container, holder, skillTypeId, event.getExperience(), Timestamp
              .from(Instant.now()))
              .build(context)
              .keepStatement(false));
        } else {
          batchUpdate.add(createInsertSkillExperienceQuery(container, holder, skillTypeId, event.getExperience())
              .build(context)
              .keepStatement(false));
        }

        preEvents.add(event);

        context.batch(batchInsert).execute();

        context.batch(batchUpdate).execute();

        Sponge.getScheduler().createTaskBuilder()
            .name(this.container.getName() + " - Fire Save Post Events [" + container + " | " + holder + "]")
            .execute(() -> {
              for (ExperienceEvent.Save.Pre preEvent : preEvents) {
                final SkillHolder skillHolder = SkillManagerImpl.this.getHolder(preEvent.getContainerUniqueId(), preEvent
                    .getHolderUniqueId()).orElse(null);

                if (skillHolder == null) {
                  continue;
                }

                final Skill skill = skillHolder.getSkill(preEvent.getSkillType()).orElse(null);

                if (skill == null) {
                  continue;
                }

                Sponge.getCauseStackManager().pushCause(SkillManagerImpl.this);
                Sponge.getEventManager().post(new SaveExperiencePostEventImpl(skill, preEvent.getOriginalExperience(), preEvent
                    .getExperience()));
                Sponge.getCauseStackManager().popCause();

                skill.setDirtyState(false);
              }
            })
            .submit(this.container);
      }
    } catch (final SQLException e) {
      e.printStackTrace();
    }
  }

  private void loadContainer(final UUID container) {
    final SaveContainerToDatabase runnable = new SaveContainerToDatabase(this, container);

    final Task task = Sponge.getScheduler().createTaskBuilder()
        .name(this.container.getName() + " - Save Skills in Container [" + container + "]")
        .async()
        .interval(60, TimeUnit.SECONDS)
        .execute(runnable)
        .submit(this.container);

    this.savingTasks.put(container, task);
    this.containerRunnables.put(container, runnable);
  }

  private void unloadContainer(final UUID container) {
    final Task scheduled = this.savingTasks.remove(container);

    if (scheduled != null) {
      // Cancel running tasks
      scheduled.cancel();

      // Remove all holders in the world
      final Set<SkillHolder> holders = this.containerHolders.remove(container);

      if (holders != null) {
        // Send an immediate task to save any holders we have in the world
        Sponge.getScheduler().createTaskBuilder()
            .name(this.container.getName() + " - Save Skills in Container [" + container + "]")
            .async()
            .execute(new SaveContainerToDatabase(this, container))
            .submit(this.container);
      }
    }

    this.containerRunnables.remove(container);
  }

  void queueToSave(final SkillHolder holder) {

    final UUID containerUniqueId = holder.getContainerUniqueId();
    final UUID holderUniqueId = holder.getHolderUniqueId();

    final SaveContainerToDatabase queueable = this.containerRunnables.get(containerUniqueId);

    if (queueable != null) {

      final Map<String, Double> dirtySkills = new HashMap<>();

      for (final Map.Entry<SkillType, Skill> skillEntry : holder.getSkills().entrySet()) {
        final SkillType skillType = skillEntry.getKey();
        final Skill skill = skillEntry.getValue();

        if (skill.isInitialized() && skill.isDirtyState()) {
          dirtySkills.put(skillType.getId(), skill.getCurrentExperience());
        }
      }

      if (!dirtySkills.isEmpty()) {
        queueable.queue(holderUniqueId, dirtySkills);
      }
    }
  }

  private static final class SaveContainerToDatabase implements Runnable {

    private final SkillManagerImpl manager;
    private final UUID container;

    private final Queue<DirtySkillHolderQueueEntry> queued = new ConcurrentLinkedQueue<>();

    SaveContainerToDatabase(final SkillManagerImpl manager, final UUID container) {
      this.manager = manager;
      this.container = container;
    }

    @Override
    public void run() {

      DirtySkillHolderQueueEntry entry;

      while ((entry = this.queued.poll()) != null) {
        this.manager.saveSkillsAsync(this.container, entry.holder, entry.dirtySkills);
      }
    }

    public void queue(final UUID holder, final Map<String, Double> dirtySkills) {
      final DirtySkillHolderQueueEntry queueEntry = new DirtySkillHolderQueueEntry(holder, dirtySkills);

      // Only persist the current queue element
      this.queued.remove(queueEntry);
      this.queued.add(queueEntry);
    }
  }

  private static final class SaveHolderToDatabase implements Runnable {

    private final SkillManagerImpl manager;
    private final UUID container;
    private final DirtySkillHolderQueueEntry entry;

    SaveHolderToDatabase(final SkillManagerImpl manager, final UUID container, final DirtySkillHolderQueueEntry entry) {
      this.manager = manager;
      this.container = container;
      this.entry = entry;
    }

    @Override
    public void run() {
      this.manager.saveSkillsAsync(this.container, this.entry.holder, this.entry.dirtySkills);
    }
  }

  private static final class DirtySkillHolderQueueEntry {

    final UUID holder;
    final Map<String, Double> dirtySkills = new HashMap<>();

    private DirtySkillHolderQueueEntry(final UUID holder, final Map<String, Double> dirtySkills) {
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
}
