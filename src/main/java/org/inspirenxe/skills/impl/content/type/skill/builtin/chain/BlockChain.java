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
package org.inspirenxe.skills.impl.content.type.skill.builtin.chain;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.util.function.TriFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.event.cause.Cause;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * A rule matching block states.
 *
 * <p>Matching modes:</p>
 * <ul>
 *     <li>{@link #matchTypeOnly()}: a state matches if its {@link BlockType} equals the type of any queried state.</li>
 *     <li>{@link #fuzzyMatch()} (exact state, the default): a state matches if it equals any queried state, comparing every trait except
 *     the traits named via {@link #ignoreTraits(BlockTrait[])} / {@link #ignoreTraits(String...)} / {@link #queryIgnoring(BlockState,
 *     BlockTrait[])}. Ignored traits apply to every queried state of this chain (and are copied by {@link #from(BlockChain)}), so e.g.
 *     {@code query(oakLog).ignoreTraits(EnumTraits.LOG_AXIS)} matches oak logs on every axis while still requiring variant=oak.</li>
 * </ul>
 *
 * <p>Any number of states may be queried, either by calling {@code query} repeatedly or by passing several states at once.</p>
 */
@SuppressWarnings("unchecked")
public final class BlockChain extends Chain<BlockChain> {

    private static final Logger logger = LoggerFactory.getLogger(SkillsImpl.ID);
    public List<BlockState> toQuery = new ArrayList<>();
    public boolean inverseQuery = false, matchOnlyType = false;
    @Nullable public TriFunction<Cause, Skill, BlockSnapshot, Boolean> owner;
    /**
     * Names of block traits ignored when matching exact states (see class docs).
     */
    public Set<String> ignoredTraits = new HashSet<>();

    public BlockChain inverseQuery() {
        if (this.inErrorState) {
            return this;
        }
        this.inverseQuery = true;
        return this;
    }

    public BlockChain query(final String id) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(id);
        final BlockType blockType = Sponge.getRegistry().getType(BlockType.class, id).orElse(null);
        if (blockType == null) {
            logger.error("Unknown block id '" + id + "' given to block chain!");
            this.inErrorState = true;
            return this;
        }
        this.toQuery.add(blockType.getDefaultState());
        return this;
    }

    public BlockChain queryDomain(final String domain) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(domain);
        Sponge.getRegistry().getAllFor(domain, BlockType.class).forEach(type -> this.toQuery.add(type.getDefaultState()));
        return this;
    }

    public BlockChain query(final BlockType value) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(value);
        this.toQuery.add(value.getDefaultState());
        return this;
    }

    public BlockChain query(@Nullable final BlockState value) {
        if (this.inErrorState) {
            return this;
        }
        if (value == null) {
            // Typically the result of a failed BlockState#withTrait(...).orElse(null)
            logger.error("Null block state given to block chain!");
            this.inErrorState = true;
            return this;
        }
        this.toQuery.add(value);
        return this;
    }

    public BlockChain query(final BlockState... values) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(values);
        for (final BlockState value : values) {
            this.query(value);
        }
        return this;
    }

    /**
     * Queries the given state and ignores the given traits when matching exact states. Equivalent to
     * {@code query(value).ignoreTraits(traits)}; note that ignored traits apply to every queried state of this chain.
     */
    public BlockChain queryIgnoring(@Nullable final BlockState value, final BlockTrait<?>... traits) {
        return this.query(value).ignoreTraits(traits);
    }

    /**
     * Ignores the given traits (matched by name) when matching exact states, e.g. log axis or cocoa facing.
     */
    public BlockChain ignoreTraits(final BlockTrait<?>... traits) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(traits);
        for (final BlockTrait<?> trait : traits) {
            checkNotNull(trait);
            this.ignoredTraits.add(trait.getName());
        }
        return this;
    }

    /**
     * Ignores the traits with the given names when matching exact states, e.g. "axis" or "facing".
     */
    public BlockChain ignoreTraits(final String... traitNames) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(traitNames);
        for (final String name : traitNames) {
            checkNotNull(name);
            this.ignoredTraits.add(name);
        }
        return this;
    }

    public BlockChain matchTypeOnly() {
        if (this.inErrorState) {
            return this;
        }
        this.matchOnlyType = true;
        return this;
    }

    public BlockChain fuzzyMatch() {
        if (this.inErrorState) {
            return this;
        }
        this.matchOnlyType = false;
        return this;
    }

    public BlockChain creator(final TriFunction<Cause, Skill, BlockSnapshot, Boolean> value) {
        if (this.inErrorState) {
            return this;
        }
        checkNotNull(value);
        this.owner = value;

        return this;
    }

    /**
     * @return True if a state of the given type could possibly match this chain (used to index chains by block type).
     */
    public boolean couldMatchType(final BlockType type) {
        if (this.inverseQuery) {
            return true;
        }
        for (final BlockState query : this.toQuery) {
            if (query.getType() == type) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return True if the given state matches this chain, honoring {@link #matchOnlyType}, {@link #ignoredTraits} and {@link #inverseQuery}.
     */
    public boolean matches(final BlockState state) {
        boolean found = false;
        for (final BlockState query : this.toQuery) {
            if (this.matchOnlyType ? query.getType() == state.getType() : this.stateMatches(query, state)) {
                found = true;
                break;
            }
        }
        return this.inverseQuery != found;
    }

    private boolean stateMatches(final BlockState query, final BlockState state) {
        if (query.getType() != state.getType()) {
            return false;
        }
        if (this.ignoredTraits.isEmpty()) {
            return query.equals(state);
        }
        final Map<BlockTrait<?>, ?> stateTraits = state.getTraitMap();
        for (final Map.Entry<BlockTrait<?>, ?> entry : query.getTraitMap().entrySet()) {
            if (this.ignoredTraits.contains(entry.getKey().getName())) {
                continue;
            }
            if (!Objects.equals(entry.getValue(), stateTraits.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BlockChain from(final BlockChain builder) {
        super.from(builder);

        if (this.inErrorState) {
            return this;
        }

        this.matchOnlyType = builder.matchOnlyType;
        this.owner = builder.owner;
        this.ignoredTraits = new HashSet<>(builder.ignoredTraits);
        return this;
    }
}
