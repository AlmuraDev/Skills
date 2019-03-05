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
import com.flowpowered.math.vector.Vector3i;
import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import org.inspirenxe.skills.generated.tables.SkillsBlockCreation;
import org.inspirenxe.skills.generated.tables.records.SkillsBlockCreationPalleteRecord;
import org.inspirenxe.skills.generated.tables.records.SkillsBlockCreationRecord;
import org.inspirenxe.skills.impl.database.DatabaseManager;
import org.inspirenxe.skills.impl.database.DatabaseQuery;
import org.inspirenxe.skills.impl.database.Queries;
import org.jooq.DSLContext;
import org.jooq.DeleteConditionStep;
import org.jooq.InsertValuesStep3;
import org.jooq.InsertValuesStep4;
import org.jooq.Query;
import org.jooq.Result;
import org.jooq.Results;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class BlockCreationTracker implements Witness {
    private final PluginContainer container;
    private final DatabaseManager databaseManager;
    private final Scheduler scheduler;

    private final Map<UUID, Long2ObjectArrayMap<BlockCreationType>> cache = new HashMap<>();

    @Inject
    public BlockCreationTracker(final PluginContainer container, final DatabaseManager databaseManager, final Scheduler scheduler) {
        this.container = container;
        this.databaseManager = databaseManager;
        this.scheduler = scheduler;
    }

    @Listener
    public void onGameStartingServer(final GameStartingServerEvent event) {
        // Yes, this needs to be on the server thread as the database must be prepped.
        try (final DSLContext context = this.databaseManager.createContext(true)) {
            final SkillsBlockCreationPalleteRecord record = Queries
                .createFetchBlockCreationPalleteQuery(BlockCreationTypes.SAPLING.getId())
                .build(context)
                .keepStatement(false)
                .fetchOne();

            if (record == null) {
                final int result = Queries
                    .createInsertBlockCreationPalleteQuery(BlockCreationTypes.SAPLING.getId(), BlockCreationTypes.SAPLING.getName())
                    .build(context)
                    .keepStatement(false)
                    .execute();

                if (result == 0) {
                    // TODO Insert failed! What to do!?
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

                final UUID container = world.getUniqueId();

                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final Results results = Queries
                        .createFetchBlockCreationQuery(container)
                        .build(context)
                        .keepStatement(false)
                        .fetchMany();

                    this.scheduler
                        .createTaskBuilder()
                        .execute(() -> {
                            final Long2ObjectArrayMap<BlockCreationType> worldCache = new Long2ObjectArrayMap<>();

                            results.forEach(r -> r.forEach(result -> {
                                final Long key = result.getValue(SkillsBlockCreation.SKILLS_BLOCK_CREATION.POS);
                                final Integer creationIndex = result.getValue(SkillsBlockCreation.SKILLS_BLOCK_CREATION.CREATION_TYPE);

                                // TODO Lookup by index
                                worldCache.put(key, BlockCreationTypes.SAPLING);
                            }));

                            this.cache.put(container, worldCache);
                        })
                        .submit(this.container);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }

    @Listener
    public void onUnloadWorld(final UnloadWorldEvent event) {

    }

    // We only care about saplings placing blocks, need to track those positions
    @Listener
    public void onChangeBlockPlace(final ChangeBlockEvent.Place event, @Root final BlockSnapshot snapshot, @First final Player player) {
        if (snapshot.getState().getType() != BlockTypes.SAPLING) {
            return;
        }

        final List<DatabaseQuery<InsertValuesStep3<SkillsBlockCreationRecord, byte[], Long, Integer>>> toInsert = new ArrayList<>();

        for (final Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (!transaction.isValid()) {
                continue;
            }

            final Location<World> location = transaction.getFinal().getLocation().orElse(null);
            if (location == null) {
                continue;
            }

            final Vector3i chunkPos = location.getChunkPosition();
            final Vector3i blockPos = location.getBlockPosition();
            final long key = this.getKey(chunkPos.getX(), chunkPos.getZ(), blockPos.getX()&0x000F, blockPos.getY()&0x00FF, blockPos.getZ()&0x000F);

            this.cache.computeIfAbsent(location.getExtent().getUniqueId(), (k) -> new Long2ObjectArrayMap<>()).put(key, BlockCreationTypes.SAPLING);
            toInsert.add(Queries.createInsertBlockCreationQuery(location.getExtent().getUniqueId(), key, BlockCreationTypes.SAPLING.getIndex()));
        }

        this.submitQueries(toInsert);
    }

    @Listener
    public void onChangeBlockBreak(final ChangeBlockEvent.Break event) {
        final List<DatabaseQuery<DeleteConditionStep<SkillsBlockCreationRecord>>> toDelete = new ArrayList<>();

        for (final Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (!transaction.isValid()) {
                continue;
            }

            final BlockSnapshot snapshot = transaction.getFinal();

            if (snapshot.getState().getType() != BlockTypes.AIR) {
                continue;
            }

            final Location<World> location = snapshot.getLocation().orElse(null);
            if (location == null) {
                continue;
            }

            final Long2ObjectArrayMap<BlockCreationType> worldCache = this.cache.get(location.getExtent().getUniqueId());
            if (worldCache == null) {
                continue;
            }

            final Vector3i chunkPos = location.getChunkPosition();
            final Vector3i blockPos = location.getBlockPosition();
            final long key = this.getKey(chunkPos.getX(), chunkPos.getZ(), blockPos.getX() & 15, blockPos.getY()&0x00FF, blockPos.getZ() & 15);
            worldCache.remove(key);

            toDelete.add(Queries.createDeleteBlockCreationQuery(location.getExtent().getUniqueId(), key));
        }

        this.submitQueries(toDelete);
    }

    @Nullable
    public BlockCreationType getCreationType(final BlockSnapshot snapshot) {
        final Location<World> location = snapshot.getLocation().orElse(null);
        if (location == null) {
            return null;
        }

        final Long2ObjectArrayMap<BlockCreationType> worldCache = this.cache.get(location.getExtent().getUniqueId());
        if (worldCache == null) {
            return null;
        }

        final Vector3i chunkPos = location.getChunkPosition();
        final Vector3i blockPos = location.getBlockPosition();
        final long key = this.getKey(chunkPos.getX(), chunkPos.getZ(), blockPos.getX() & 15, blockPos.getY()&0x00FF, blockPos.getZ() & 15);
        return worldCache.get(key);
    }

    private long getKey(int cx, int cz, int bx, int by, int bz) {
        final int packedXZ = (bx << 4 | bz);
        final int packedPos = by << 8 | packedXZ;

        return cx << 38 | cz << 16 | packedPos;
    }

    private void submitQueries(final List<? extends DatabaseQuery<?>> builders) {
        this.scheduler
            .createTaskBuilder()
            .async()
            .execute(() -> {
                try (final DSLContext context = this.databaseManager.createContext(true)) {
                    final List<Query> queries = new ArrayList<>();
                    builders.forEach(builder -> queries.add(builder.build(context).keepStatement(false)));
                    context.batch(queries).execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            })
            .submit(this.container);
    }
}
