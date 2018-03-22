package org.inspirenxe.skills.impl.skill;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.inspirenxe.skills.impl.database.Queries.createFetchExperienceQuery;
import static org.inspirenxe.skills.impl.database.Queries.createHasExperienceInSkillQuery;
import static org.inspirenxe.skills.impl.database.Queries.createInsertSkillExperienceQuery;
import static org.inspirenxe.skills.impl.database.Queries.createUpdateSkillExperienceQuery;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillManager;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.inspirenxe.skills.impl.Constants;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.database.DatabaseManager;
import org.inspirenxe.skills.impl.database.generated.tables.TblSkillExperience;
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
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.sql.Connection;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class SkillManagerImpl implements SkillManager {

    private final Map<UUID, Set<SkillHolder>> holdersInContainer = new HashMap<>();
    private final Map<UUID, Task> savingTasks = new HashMap<>();
    private final Map<UUID, SaveContainerToDatabase> queueables = new HashMap<>();

    public SkillManagerImpl() {
        // TODO Need kashike's fancy system
        Sponge.getEventManager().registerListeners(SkillsImpl.instance.container, this);
    }

    @Override
    public Set<SkillHolder> getHoldersInContainer(UUID containerUniqueId) {
        checkNotNull(containerUniqueId);

        Set<SkillHolder> holders = this.holdersInContainer.get(containerUniqueId);
        if (holders == null) {
            holders = new HashSet<>();
        }

        return Collections.unmodifiableSet(holders);
    }

    @Override
    public Optional<SkillHolder> getHolder(UUID containerUniqueId, UUID holderUniqueId) {
        final Set<SkillHolder> holders = this.holdersInContainer.get(containerUniqueId);

        SkillHolder found = null;

        if (holders != null) {
            for (SkillHolder holder : holders) {
                if (holder.getHolderUniqueId().equals(holderUniqueId)) {
                    found = holder;
                    break;
                }
            }
        }

        return Optional.ofNullable(found);
    }

    @Listener
    public void onUnloadWorld(UnloadWorldEvent event) {
        this.unloadContainer(event.getTargetWorld().getUniqueId());
    }

    @Listener(order = Order.LAST)
    public void onClientConnectionJoinByPlayer(ClientConnectionEvent.Join event, @Root Player player) {
        final UUID containerUniqueId = player.getWorld().getUniqueId();
        final UUID holderUniqueId = player.getUniqueId();

        // TODO Throw CreateSkillHolderEvent and let the plugin control the holder implementation themselves
        final SkillHolderImpl holder = SkillHolderImpl.of(containerUniqueId, holderUniqueId);

        // Load skills for the holder in this new container
        this.holdersInContainer.computeIfAbsent(containerUniqueId, k -> {
            this.loadContainer(containerUniqueId);
            return new HashSet<>();
        }).add(holder);

        final Collection<SkillType> skillTypes = Sponge.getRegistry().getAllOf(SkillType.class);
        skillTypes.forEach(holder::addSkill);

        Sponge.getScheduler().createTaskBuilder()
                .name(Constants.Plugin.NAME + "- Load Skills [" + containerUniqueId + " | " + holderUniqueId + "]")
                .async()
                .execute(() -> this.loadSkillsAsync(holder.getContainerUniqueId(), holder.getHolderUniqueId(), Sponge.getRegistry().getAllOf
                        (SkillType.class).stream().map(CatalogType::getId).collect(Collectors.toSet())))
                .submit(SkillsImpl.instance.container);
    }

    @Listener
    public void onClientConnectionDisconnectByPlayer(ClientConnectionEvent.Disconnect event, @Root Player player) {
        final UUID containerUniqueId = player.getWorld().getUniqueId();
        final UUID holderUniqueId = player.getUniqueId();

        // Save skills for the holder in the old container and remove them
        final Set<SkillHolder> holdersInContainer = this.holdersInContainer.get(containerUniqueId);
        if (holdersInContainer != null) {
            final SkillHolder previousHolder = holdersInContainer.stream().filter((h) -> h.getHolderUniqueId().equals(holderUniqueId)).findFirst()
                    .orElse(null);

            if (previousHolder != null) {
                holdersInContainer.remove(previousHolder);

                // Remove container map if we no longer have holders
                if (holdersInContainer.isEmpty()) {
                    this.unloadContainer(containerUniqueId);
                }

                final Map<String, Double> dirtySkills = new HashMap<>();

                for (Map.Entry<SkillType, Skill> skillEntry : previousHolder.getSkills().entrySet()) {
                    final SkillType skillType = skillEntry.getKey();
                    final Skill skill = skillEntry.getValue();

                    if (skill.isInitialized() && skill.isDirtyState()) {
                        dirtySkills.put(skillType.getId(), skill.getCurrentExperience());
                    }
                }

                if (!dirtySkills.isEmpty()) {
                    Sponge.getScheduler().createTaskBuilder()
                            .name(Constants.Plugin.NAME + "- Save Skills [" + containerUniqueId + " | " + holderUniqueId + "]")
                            .async()
                            .execute(new SaveHolderToDatabase(this, containerUniqueId, new DirtySkillHolderQueueEntry(holderUniqueId, dirtySkills)))
                            .submit(SkillsImpl.instance.container);
                }
            }
        }
    }

    @Listener(order = Order.LAST)
    public void onMoveTeleportByPlayer(MoveEntityEvent.Teleport event, @Getter("getTargetEntity") Player player) {
        final UUID previousContainerUniqueId = event.getFromTransform().getExtent().getUniqueId();
        final UUID holderUniqueId = event.getTargetEntity().getUniqueId();
        final UUID containerUniqueId = event.getToTransform().getExtent().getUniqueId();

        // Detect world change, if not bail
        if (previousContainerUniqueId.equals(containerUniqueId)) {
            return;
        }

        // Save skills for the holder in the old container and remove them
        final Set<SkillHolder> holdersInContainer = this.holdersInContainer.get(previousContainerUniqueId);
        if (holdersInContainer != null) {
            final SkillHolder previousHolder = holdersInContainer.stream().filter((h) -> h.getHolderUniqueId().equals(holderUniqueId)).findFirst()
                    .orElse(null);

            if (previousHolder != null) {
                holdersInContainer.remove(previousHolder);

                // Remove container map if we no longer have holders
                if (holdersInContainer.isEmpty()) {
                    this.unloadContainer(previousContainerUniqueId);
                }

                final Map<String, Double> dirtySkills = new HashMap<>();

                for (Map.Entry<SkillType, Skill> skillEntry : previousHolder.getSkills().entrySet()) {
                    final SkillType skillType = skillEntry.getKey();
                    final Skill skill = skillEntry.getValue();

                    if (skill.isInitialized() && skill.isDirtyState()) {
                        dirtySkills.put(skillType.getId(), skill.getCurrentExperience());
                    }
                }

                if (!dirtySkills.isEmpty()) {
                    Sponge.getScheduler().createTaskBuilder()
                            .name(Constants.Plugin.NAME + "- Save Skills [" + previousContainerUniqueId + " | " + holderUniqueId + "]")
                            .async()
                            .execute(new SaveHolderToDatabase(this, previousContainerUniqueId, new DirtySkillHolderQueueEntry(holderUniqueId,
                                    dirtySkills)))
                            .submit(SkillsImpl.instance.container);
                }
            }
        }


        // TODO Throw CreateSkillHolderEvent and let the plugin control the holder implementation themselves
        final SkillHolderImpl holder = SkillHolderImpl.of(containerUniqueId, holderUniqueId);

        // Load skills for the holder in this new container
        // Load skills for the holder in this new container
        this.holdersInContainer.computeIfAbsent(containerUniqueId, k -> {
            this.loadContainer(containerUniqueId);
            return new HashSet<>();
        }).add(holder);

        final Collection<SkillType> skillTypes = Sponge.getRegistry().getAllOf(SkillType.class);
        skillTypes.forEach(holder::addSkill);

        Sponge.getScheduler().createTaskBuilder()
                .name(Constants.Plugin.NAME + "- Load Skills [" + containerUniqueId + " | " + holderUniqueId + "]")
                .async()
                .execute(() -> this.loadSkillsAsync(holder.getContainerUniqueId(), holder.getHolderUniqueId(), skillTypes.stream().map(
                        CatalogType::getId).collect(Collectors.toSet())))
                .submit(SkillsImpl.instance.container);
    }

    private void loadSkillsAsync(UUID containerUniqueId, UUID holderUniqueId, Collection<String> skillTypeIds) {

        final PluginContainer container = SkillsImpl.instance.container;
        final ExecutorService executor = SkillsImpl.instance.asyncExecutor;
        final DatabaseManager manager = SkillsImpl.instance.databaseManager;

        // Commence loading of skills
        try (DSLContext context = manager.createContext()) {
            for (String skillTypeId : skillTypeIds) {
                createFetchExperienceQuery(containerUniqueId, holderUniqueId, skillTypeId)
                        .build(context)
                        .keepStatement(false)
                        .fetchAsync(executor)
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
                                dbExperience = rows.getValue(0, TblSkillExperience.TBL_SKILL_EXPERIENCE.EXPERIENCE).doubleValue();
                                isNewSkill = false;
                            }

                            final Cause cause = Cause.of(EventContext.empty(), SkillsImpl.instance.container, this);
                            final ExperienceEvent.Load.Pre event = new LoadExperiencePreEventImpl(cause, containerUniqueId, holderUniqueId,
                                    skillType, dbExperience, dbExperience, !isNewSkill);
                            Sponge.getEventManager().post(event);

                            final double experience = dbExperience;

                            Sponge.getScheduler().createTaskBuilder()
                                    .name(Constants.Plugin.ID + " - Apply Skill " + skillType.getName() + " Experience For [" + containerUniqueId +
                                            " | " + holderUniqueId + "]")
                                    .execute(() -> {
                                        final SkillHolder applyHolder = SkillManagerImpl.this.getHolder(containerUniqueId, holderUniqueId).orElse
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
                                    .submit(container);
                        });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveSkillsAsync(UUID containerUniqueId, UUID holderUniqueId, Map<String, Double> skillTypesByExperience) throws SQLException {

        final DatabaseManager manager = SkillsImpl.instance.databaseManager;

        final HashSet<Query> batchInsert = new HashSet<>();
        final HashSet<Query> batchUpdate = new HashSet<>();

        final HashSet<ExperienceEvent.Save.Pre> preEvents = new HashSet<>();

        try (DSLContext context = manager.createContext()) {
            for (Map.Entry<String, Double> skillEntry : skillTypesByExperience.entrySet()) {
                final String skillTypeId = skillEntry.getKey();
                final SkillType skillType = Sponge.getRegistry().getType(SkillType.class, skillTypeId).orElse(null);

                if (skillType == null) {
                    continue;
                }

                final double skillExperience = skillEntry.getValue();

                final Cause cause = Cause.of(EventContext.empty(), SkillsImpl.instance.container, this);
                final ExperienceEvent.Save.Pre event = new SaveExperiencePreEventImpl(cause, containerUniqueId, holderUniqueId,
                        skillType, skillExperience, skillExperience);
                Sponge.getEventManager().post(event);

                final SelectConditionStep<Record1<Integer>> hasExperienceInSkillQuery = createHasExperienceInSkillQuery(containerUniqueId,
                        holderUniqueId, skillTypeId).build(context);

                final int result = hasExperienceInSkillQuery
                        .keepStatement(false)
                        .execute();

                if (result == 1) {
                    batchInsert.add(createUpdateSkillExperienceQuery(containerUniqueId, holderUniqueId, skillTypeId, event.getExperience(), Timestamp
                            .from(Instant.now()))
                            .build(context)
                            .keepStatement(false));
                } else {
                    batchUpdate.add(createInsertSkillExperienceQuery(containerUniqueId, holderUniqueId, skillTypeId, event.getExperience())
                            .build(context)
                            .keepStatement(false));
                }

                preEvents.add(event);

                context.batch(batchInsert).execute();

                context.batch(batchUpdate).execute();

                Sponge.getScheduler().createTaskBuilder()
                        .name(Constants.Plugin.NAME + " - Fire Save Post Events [" + containerUniqueId + " | " + holderUniqueId + "]")
                        .execute(() -> {
                            for (ExperienceEvent.Save.Pre preEvent : preEvents) {
                                final SkillHolder skillHolder = SkillManagerImpl.this.getHolder(preEvent.getContainerUniqueId(), preEvent
                                        .getHolderUniqueId()).orElse(null);

                                if (skillHolder == null) {
                                    continue;
                                }

                                final Skill skill = skillHolder.getSkill(preEvent.getTargetSkillType()).orElse(null);

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
                        .submit(SkillsImpl.instance.container);
            }
        }
    }

    private void loadContainer(UUID containerUniqueId) {
        final SaveContainerToDatabase queueable = new SaveContainerToDatabase(this, containerUniqueId);

        final Task task = Sponge.getScheduler().createTaskBuilder()
                .name(Constants.Plugin.NAME + " - Save Skills in Container [" + containerUniqueId + "]")
                .async()
                .interval(60, TimeUnit.SECONDS)
                .execute(queueable)
                .submit(SkillsImpl.instance.container);

        this.savingTasks.put(containerUniqueId, task);
        this.queueables.put(containerUniqueId, queueable);
    }

    private void unloadContainer(UUID containerUniqueId) {
        final Task scheduled = this.savingTasks.remove(containerUniqueId);

        if (scheduled != null) {
            // Cancel running tasks
            scheduled.cancel();

            // Remove all holders in the world
            final Set<SkillHolder> holders = this.holdersInContainer.remove(containerUniqueId);

            if (holders != null) {

                // Send an immediate task to save any holders we have in the world
                Sponge.getScheduler().createTaskBuilder()
                        .name(Constants.Plugin.NAME + " - Save Skills in Container [" + containerUniqueId + "]")
                        .async()
                        .execute(new SaveContainerToDatabase(this, containerUniqueId))
                        .submit(SkillsImpl.instance.container);
            }
        }

        this.queueables.remove(containerUniqueId);
    }

    void queueToSave(SkillHolder holder) {

        final UUID containerUniqueId = holder.getContainerUniqueId();
        final UUID holderUniqueId = holder.getHolderUniqueId();

        final SaveContainerToDatabase queueable = this.queueables.get(containerUniqueId);

        if (queueable != null) {

            final Map<String, Double> dirtySkills = new HashMap<>();

            for (Map.Entry<SkillType, Skill> skillEntry : holder.getSkills().entrySet()) {
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
        private final UUID containerUniqueId;

        private final Queue<DirtySkillHolderQueueEntry> queued = new ConcurrentLinkedQueue<>();

        SaveContainerToDatabase(SkillManagerImpl manager, UUID containerUniqueId) {
            this.manager = manager;
            this.containerUniqueId = containerUniqueId;
        }

        @Override
        public void run() {

            DirtySkillHolderQueueEntry entry;

            while ((entry = queued.poll()) != null) {

                try {
                    manager.saveSkillsAsync(containerUniqueId, entry.holderUniqueId, entry.dirtySkills);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        public void queue(UUID holderUniqueId, Map<String, Double> dirtySkills) {
            final DirtySkillHolderQueueEntry queueEntry = new DirtySkillHolderQueueEntry(holderUniqueId, dirtySkills);

            // Only persist the current queue element
            this.queued.remove(queueEntry);
            this.queued.add(queueEntry);
        }
    }

    private static final class SaveHolderToDatabase implements Runnable {

        private final SkillManagerImpl manager;
        private final UUID containerUniqueId;
        private final DirtySkillHolderQueueEntry queueEntry;

        SaveHolderToDatabase(SkillManagerImpl manager, UUID containerUniqueId, DirtySkillHolderQueueEntry queueEntry) {
            this.manager = manager;
            this.containerUniqueId = containerUniqueId;
            this.queueEntry = queueEntry;
        }

        @Override
        public void run() {

            try {
                this.manager.saveSkillsAsync(this.containerUniqueId, this.queueEntry.holderUniqueId, this.queueEntry.dirtySkills);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static final class DirtySkillHolderQueueEntry {
        final UUID holderUniqueId;
        final Map<String, Double> dirtySkills = new HashMap<>();

        private DirtySkillHolderQueueEntry(UUID holderUniqueId, Map<String, Double> dirtySkills) {
            this.holderUniqueId = holderUniqueId;
            this.dirtySkills.putAll(dirtySkills);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DirtySkillHolderQueueEntry)) {
                return false;
            }
            final DirtySkillHolderQueueEntry entry = (DirtySkillHolderQueueEntry) o;
            return Objects.equals(this.holderUniqueId, entry.holderUniqueId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.holderUniqueId);
        }
    }
}
