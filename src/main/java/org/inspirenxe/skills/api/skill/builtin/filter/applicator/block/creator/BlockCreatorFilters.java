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
package org.inspirenxe.skills.api.skill.builtin.filter.applicator.block.creator;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.api.event.BlockCreationFlags;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.builtin.applicator.Applicator;
import org.inspirenxe.skills.api.skill.builtin.filter.FilterResult;
import org.inspirenxe.skills.api.skill.builtin.query.block.BlockQuery;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class BlockCreatorFilters {

    public static BlockCreatorFilter creatorOnly(final BlockQuery query) {
        checkNotNull(query);

        return new BlockCreatorFilter<BlockCreatorFilter>() {

            final List<Applicator<BlockSnapshot, BlockQuery>> applicators = new ArrayList<>();

            @Override
            public BlockCreatorFilter then(final Applicator<BlockSnapshot, BlockQuery>... applicators) {
                this.applicators.addAll(Arrays.asList(applicators));
                return this;
            }

            @Override
            public List<Applicator<BlockSnapshot, BlockQuery>> getApplicators() {
                return this.applicators;
            }

            @Override
            public FilterResult test(final Cause cause, final Skill skill, final BlockSnapshot snapshot, final Set<BlockCreationFlags> flags) {
                if (!this.getQuery().matches(snapshot)) {
                    return FilterResult.SKIP;
                }

                final UUID creator = snapshot.getCreator().orElse(null);
                if (creator == null) {
                    return FilterResult.FAIL;
                }

                final User user = cause.first(User.class).orElse(null);
                if (user == null) {
                    return FilterResult.FAIL;
                }

                if (!user.getUniqueId().equals(creator)) {
                    return FilterResult.FAIL;
                }

                return FilterResult.SUCCESS;
            }

            @Override
            public BlockQuery getQuery() {
                return query;
            }
        };
    }

    public static BlockCreatorFilter creatorOrNatural(final BlockQuery query) {
        checkNotNull(query);

        return new BlockCreatorFilter<BlockCreatorFilter>() {

            final List<Applicator<BlockSnapshot, BlockQuery>> applicators = new ArrayList<>();

            @Override
            public BlockCreatorFilter then(final Applicator<BlockSnapshot, BlockQuery>... applicators) {
                this.applicators.addAll(Arrays.asList(applicators));
                return this;
            }

            @Override
            public List<Applicator<BlockSnapshot, BlockQuery>> getApplicators() {
                return this.applicators;
            }

            @Override
            public FilterResult test(final Cause cause, final Skill skill, final BlockSnapshot snapshot, final Set<BlockCreationFlags> flags) {
                if (!this.getQuery().matches(snapshot)) {
                    return FilterResult.SKIP;
                }

                final UUID creator = snapshot.getCreator().orElse(null);
                if (creator == null) {
                    return FilterResult.SUCCESS;
                }

                final User user = cause.first(User.class).orElse(null);
                if (user == null) {
                    return FilterResult.FAIL;
                }

                if (!user.getUniqueId().equals(creator)) {
                    return FilterResult.FAIL;
                }

                return FilterResult.SUCCESS;
            }

            @Override
            public BlockQuery getQuery() {
                return query;
            }
        };
    }

    public static BlockCreatorFilter creatorTrackedOrNatural(final BlockQuery query) {
        checkNotNull(query);

        return new BlockCreatorFilter<BlockCreatorFilter>() {

            final List<Applicator<BlockSnapshot, BlockQuery>> applicators = new ArrayList<>();

            @Override
            public BlockCreatorFilter then(final Applicator<BlockSnapshot, BlockQuery>... applicators) {
                this.applicators.addAll(Arrays.asList(applicators));
                return this;
            }

            @Override
            public List<Applicator<BlockSnapshot, BlockQuery>> getApplicators() {
                return this.applicators;
            }

            @Override
            public FilterResult test(final Cause cause, final Skill skill, final BlockSnapshot snapshot, final Set<BlockCreationFlags> flags) {
                if (!this.getQuery().matches(snapshot)) {
                    return FilterResult.SKIP;
                }

                final UUID creator = snapshot.getCreator().orElse(null);
                if (creator == null) {
                    return FilterResult.SUCCESS;
                }

                final User user = cause.first(User.class).orElse(null);
                if (user == null) {
                    return FilterResult.FAIL;
                }

                if (!user.getUniqueId().equals(creator)) {
                    return FilterResult.FAIL;
                }

                if (flags.isEmpty()) {
                    return FilterResult.FAIL;
                }

                return FilterResult.SUCCESS;
            }

            @Override
            public BlockQuery getQuery() {
                return query;
            }
        };
    }

    public static BlockCreatorFilter natural(final BlockQuery query) {
        checkNotNull(query);

        return new BlockCreatorFilter<BlockCreatorFilter>() {

            final List<Applicator<BlockSnapshot, BlockQuery>> applicators = new ArrayList<>();

            @Override
            public BlockCreatorFilter then(final Applicator<BlockSnapshot, BlockQuery>... applicators) {
                this.applicators.addAll(Arrays.asList(applicators));
                return this;
            }

            @Override
            public List<Applicator<BlockSnapshot, BlockQuery>> getApplicators() {
                return this.applicators;
            }

            @Override
            public FilterResult test(final Cause cause, final Skill skill, final BlockSnapshot snapshot, final Set<BlockCreationFlags> flags) {
                if (!this.getQuery().matches(snapshot)) {
                    return FilterResult.SKIP;
                }

                final UUID creator = snapshot.getCreator().orElse(null);
                if (creator == null) {
                    return FilterResult.SUCCESS;
                }

                return FilterResult.FAIL;
            }

            @Override
            public BlockQuery getQuery() {
                return query;
            }
        };
    }

    public static BlockCreatorFilter notNatural(final BlockQuery query) {
        checkNotNull(query);

        return new BlockCreatorFilter<BlockCreatorFilter>() {

            final List<Applicator<BlockSnapshot, BlockQuery>> applicators = new ArrayList<>();

            @Override
            public BlockCreatorFilter then(final Applicator<BlockSnapshot, BlockQuery>... applicators) {
                this.applicators.addAll(Arrays.asList(applicators));
                return this;
            }

            @Override
            public List<Applicator<BlockSnapshot, BlockQuery>> getApplicators() {
                return this.applicators;
            }

            @Override
            public FilterResult test(final Cause cause, final Skill skill, final BlockSnapshot snapshot, final Set<BlockCreationFlags> flags) {
                if (!this.getQuery().matches(snapshot)) {
                    return FilterResult.SKIP;
                }

                final UUID creator = snapshot.getCreator().orElse(null);
                if (creator == null) {
                    return FilterResult.FAIL;
                }

                return FilterResult.SUCCESS;
            }

            @Override
            public BlockQuery getQuery() {
                return query;
            }
        };
    }

    private BlockCreatorFilters() {
    }
}
