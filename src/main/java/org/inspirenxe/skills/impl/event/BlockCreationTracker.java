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
package org.inspirenxe.skills.impl.event;

import com.almuradev.toolbox.inject.event.Witness;
import com.almuradev.toolbox.inject.event.WitnessScope;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.longs.Long2LongArrayMap;
import org.inspirenxe.skills.generated.tables.SkillsBlockCreation;
import org.inspirenxe.skills.generated.tables.records.SkillsContainerPaletteRecord;
import org.inspirenxe.skills.impl.configuration.PluginConfiguration;
import org.inspirenxe.skills.impl.database.DatabaseManager;
import org.inspirenxe.skills.impl.database.DatabaseQuery;
import org.inspirenxe.skills.impl.database.Queries;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.Results;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@WitnessScope.Sponge
public final class BlockCreationTracker implements Witness {

    private final PluginContainer container;
    private final PluginConfiguration pluginConfiguration;
    private final DatabaseManager databaseManager;
    private final Scheduler scheduler;

    private final BiMap<UUID, Integer> containerPalette = HashBiMap.create();
    private final Map<UUID, Long2LongArrayMap> containerCache = new HashMap<>();
    private final Map<UUID, NonDuplicateQueryBatcher> batchers = new HashMap<>();
    private final Map<UUID, Task> batcherTasks = new HashMap<>();

    @Inject
    public BlockCreationTracker(final PluginContainer container, final PluginConfiguration pluginConfiguration, final DatabaseManager databaseManager,
        final Scheduler scheduler) {
        this.container = container;
        this.pluginConfiguration = pluginConfiguration;
        this.databaseManager = databaseManager;
        this.scheduler = scheduler;
    }

    @Listener
    public void onLoadWorld(final LoadWorldEvent event) {
        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                final World world = event.getTargetWorld();
                if (!world.isLoaded()) {
                    return;
                }

                final UUID containerId = event.getTargetWorld().getUniqueId();
                Integer container = this.containerPalette.get(containerId);

                Results dbResults = null;

                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    if (container == null) {
                        SkillsContainerPaletteRecord record = Queries
                            .createFetchContainerPaletteQuery(containerId)
                            .build(context)
                            .keepStatement(false)
                            .fetchOne();
                        if (record == null) {
                            record = Queries
                                .createInsertContainerPaletteQuery(containerId)
                                .build(context)
                                .fetchOne();

                            if (record == null) {
                                // TODO Failed to insert into the palette! What do!?
                                throw new RuntimeException();
                            }
                        }

                        container = record.getRecNo();
                        this.containerPalette.put(containerId, container);
                    }

                    dbResults = Queries
                        .createFetchBlockCreationQuery(container)
                        .build(context)
                        .keepStatement(false)
                        .fetchMany();
                } catch (SQLException ignored) {
                }

                if (dbResults != null) {
                    final Results results = dbResults;

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            final Long2LongArrayMap worldCache = new Long2LongArrayMap();
                            worldCache.defaultReturnValue(Long.MIN_VALUE);

                            results.forEach(r -> r.forEach(result -> {
                                final Long key = result.getValue(SkillsBlockCreation.SKILLS_BLOCK_CREATION.POS);
                                final Long mask = result.getValue(SkillsBlockCreation.SKILLS_BLOCK_CREATION.MASK);
                                worldCache.put(key, mask);
                            }));

                            this.containerCache.put(containerId, worldCache);
                            final NonDuplicateQueryBatcher batcher = new NonDuplicateQueryBatcher(this.databaseManager);
                            this.batchers.put(containerId, batcher);
                            this.batcherTasks.put(containerId, this.scheduler
                                .createTaskBuilder()
                                .async()
                                .execute(batcher)
                                .interval(this.pluginConfiguration.getSaveInterval(), TimeUnit.SECONDS)
                                .submit(this.container)
                            );

                        })
                        .submit(this.container);
                }
            })
            .submit(this.container);
    }

    @Listener
    public void onUnloadWorld(final UnloadWorldEvent event) {
        this.containerCache.remove(event.getTargetWorld().getUniqueId());
        final Task remove = this.batcherTasks.remove(event.getTargetWorld().getUniqueId());
        if (remove != null) {
            remove.cancel();
        }
        final NonDuplicateQueryBatcher batcher = this.batchers.get(event.getTargetWorld().getUniqueId());
        if (batcher != null) {
            batcher.run();
        }
    }

    @Listener
    public void onChangeBlockPost(final ChangeBlockEvent.Post event) {
        final ChangeBlockEvent.Break breakEvent = event.getCause().first(ChangeBlockEvent.Break.class).orElse(null);
        final ChangeBlockEvent.Decay decayEvent = event.getCause().first(ChangeBlockEvent.Decay.class).orElse(null);
        final ChangeBlockEvent.Place placeEvent = event.getCause().first(ChangeBlockEvent.Place.class).orElse(null);

        if (breakEvent != null) {
            this.handleDeletions(breakEvent);
        }

        if (decayEvent != null) {
            this.handleDeletions(decayEvent);
        }

        if (placeEvent != null) {
            if (!event.getContext().get(EventContextKeys.OWNER).isPresent()) {
                return;
            }

            final Set<BlockCreationFlags> flags = BlockCreationFlags.getFlags(placeEvent.getCause(),  placeEvent.getContext());
            if (flags.isEmpty()) {
                return;
            }

            final long mask = BlockCreationFlags.mask(flags);

            for (final Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                if (!transaction.isValid()) {
                    continue;
                }

                final Location<World> location = transaction.getFinal().getLocation().orElse(null);
                if (location == null) {
                    continue;
                }

                final NonDuplicateQueryBatcher batcher = this.batchers.get(location.getExtent().getUniqueId());

                final Integer container = this.containerPalette.get(location.getExtent().getUniqueId());
                if (container == null) {
                    continue;
                }

                final Vector3i chunkPos = location.getChunkPosition();
                final Vector3i blockPos = location.getBlockPosition();
                final long key = this.getKey(chunkPos, blockPos);

                if (this.containerCache.computeIfAbsent(location.getExtent().getUniqueId(), k -> {
                    final Long2LongArrayMap map = new Long2LongArrayMap();
                    map.defaultReturnValue(Long.MIN_VALUE);
                    return map;
                }).put(key, mask) == Long.MIN_VALUE) {
                    batcher.queueQuery(key, Queries.createInsertBlockCreationQuery(container, key, mask));
                }
            }
        }
    }

    private void handleDeletions(final ChangeBlockEvent event) {
        for (final Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (!transaction.isValid()) {
                continue;
            }

            final Location<World> location = transaction.getFinal().getLocation().orElse(null);
            if (location == null) {
                continue;
            }

            final Long2LongArrayMap worldCache = this.containerCache.get(location.getExtent().getUniqueId());
            if (worldCache == null) {
                continue;
            }

            final NonDuplicateQueryBatcher batcher = this.batchers.get(location.getExtent().getUniqueId());

            final Integer container = this.containerPalette.get(location.getExtent().getUniqueId());
            if (container == null) {
                continue;
            }

            final Vector3i chunkPos = location.getChunkPosition();
            final Vector3i blockPos = location.getBlockPosition();
            final long key = this.getKey(chunkPos, blockPos);

            if (worldCache.remove(key) != Long.MIN_VALUE) {
                batcher.queueQuery(key, Queries.createDeleteBlockCreationQuery(container, key));
            }
        }
    }

    public Set<BlockCreationFlags> getCreationFlags(final BlockSnapshot snapshot) {
        final Location<World> location = snapshot.getLocation().orElse(null);
        if (location == null) {
            return Collections.emptySet();
        }

        final Long2LongArrayMap worldCache = this.containerCache.get(location.getExtent().getUniqueId());
        if (worldCache == null) {
            return Collections.emptySet();
        }

        final Vector3i chunkPos = location.getChunkPosition();
        final Vector3i blockPos = location.getBlockPosition();
        final long key = this.getKey(chunkPos, blockPos);
        final long mask = worldCache.get(key);

        if (mask == Long.MIN_VALUE) {
            return Collections.emptySet();
        }

        return BlockCreationFlags.unmask(mask);
    }

    private long getKey(Vector3i chunkPos, Vector3i blockPos) {
        final int cx = chunkPos.getX();
        final int cz = chunkPos.getZ();
        final int bx = blockPos.getX() & 15;
        final int by = blockPos.getY() & 255;
        final int bz = blockPos.getZ() & 15;

        return cx << 42 | cz << 20 | by << 8 | (bx << 4 | bz);
    }

    private final class NonDuplicateQueryBatcher implements Runnable {

        private final DatabaseManager databaseManager;
        private final ConcurrentHashMap<Long, DatabaseQuery<?>> queries = new ConcurrentHashMap<>();

        NonDuplicateQueryBatcher(DatabaseManager databaseManager) {
            this.databaseManager = databaseManager;
        }

        @Override
        public void run() {

            if (this.queries.isEmpty()) {
                return;
            }

            final Iterator<Map.Entry<Long, DatabaseQuery<?>>> iter = this.queries.entrySet().iterator();
            final List<Query> toBatch = new ArrayList<>();

            try (final DSLContext context = this.databaseManager.createContext(true)) {
                while (iter.hasNext()) {
                    final Map.Entry<Long, DatabaseQuery<?>> next = iter.next();
                    toBatch.add(next.getValue().build(context).keepStatement(false));
                    iter.remove();
                }

                if (!toBatch.isEmpty()) {
                    context.batch(toBatch).execute();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        void queueQuery(long pos, DatabaseQuery<?> query) {
            this.queries.put(pos, query);
        }
    }
}
