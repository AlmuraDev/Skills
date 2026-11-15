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
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillHolderContainer;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.Skills;
import org.inspirenxe.skills.database.Database;
import org.spongepowered.api.ResourceKey;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class SkillServiceImpl implements SkillService {

    // DecimalFormat is not thread-safe, cache one per thread instead of creating one per call
    private static final ThreadLocal<DecimalFormat> XP_FORMAT = ThreadLocal.withInitial(() -> new DecimalFormat("#,###.##"));

    private final Logger logger;
    private final Database database;
    private final List<SkillType> skillTypes;
    private final Map<ResourceKey, ResourceKey> shares;
    private final Map<ResourceKey, SkillHolderContainer> containers = new ConcurrentHashMap<>();

    /**
     * @param shares World key to the world key whose skills it uses
     */
    public SkillServiceImpl(final Logger logger, final Database database, final Collection<SkillType> skillTypes,
            final Map<ResourceKey, ResourceKey> shares) {
        this.logger = logger;
        this.database = database;
        final List<SkillType> sorted = new ArrayList<>(skillTypes);
        sorted.sort(Comparator.comparing(SkillType::name, String.CASE_INSENSITIVE_ORDER));
        this.skillTypes = Collections.unmodifiableList(sorted);
        this.shares = Map.copyOf(shares);
    }

    @Override
    public DecimalFormat xpFormat() {
        return SkillServiceImpl.XP_FORMAT.get();
    }

    @Override
    public Collection<SkillType> skillTypes() {
        return this.skillTypes;
    }

    @Override
    public Optional<SkillType> skillType(final String id) {
        Objects.requireNonNull(id, "id");
        final String lower = id.toLowerCase(Locale.ROOT);
        final String full = lower.indexOf(':') < 0 ? Skills.ID + ":" + lower : lower;
        return this.skillTypes.stream().filter(type -> type.key().asString().equals(full)).findFirst();
    }

    @Override
    public Optional<SkillHolderContainer> parentContainer(final SkillHolderContainer container) {
        Objects.requireNonNull(container, "container");
        final ResourceKey share = this.shares.get(container.key());
        if (share == null || share.equals(container.key())) {
            return Optional.of(container);
        }
        return Optional.ofNullable(this.containers.get(share));
    }

    @Override
    public Map<ResourceKey, SkillHolderContainer> containers() {
        return Collections.unmodifiableMap(this.containers);
    }

    @Override
    public SkillHolderContainer createContainer(final ResourceKey worldKey) {
        Objects.requireNonNull(worldKey, "worldKey");
        return this.containers.computeIfAbsent(worldKey, key -> new SkillHolderContainerImpl(this.logger, this.database, () -> this.skillTypes,
                key));
    }

    @Override
    public void saveContainer(final ResourceKey worldKey) {
        this.container(worldKey).ifPresent(SkillHolderContainer::save);
    }

    @Override
    public Optional<SkillHolderContainer> removeContainer(final ResourceKey worldKey) {
        Objects.requireNonNull(worldKey, "worldKey");
        return Optional.ofNullable(this.containers.remove(worldKey));
    }

    /**
     * Saves every holder of every container (snapshotted on the server thread, written on the database thread).
     */
    public void saveAll() {
        for (final SkillHolderContainer container : this.containers.values()) {
            for (final SkillHolder holder : container.holders().values()) {
                holder.save();
            }
        }
    }
}
