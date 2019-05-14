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
package org.inspirenxe.skills.impl.skill.builtin.event.processor;

import static net.kyori.filter.FilterResponse.DENY;
import static org.inspirenxe.skills.api.skill.builtin.RegistrarTypes.CANCEL_EVENT;
import static org.inspirenxe.skills.api.skill.builtin.RegistrarTypes.CANCEL_TRANSACTION;
import static org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys.BLOCK_CREATION_TRACKER;
import static org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys.PROCESSING_PLAYER;
import static org.inspirenxe.skills.api.skill.builtin.TriggerRegistrarTypes.EVENT;
import static org.inspirenxe.skills.api.skill.builtin.TriggerRegistrarTypes.TRANSACTION;

import net.kyori.filter.Filter;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.builtin.BasicSkillType;
import org.inspirenxe.skills.api.skill.builtin.FilterRegistrar;
import org.inspirenxe.skills.api.skill.builtin.filter.applicator.TriggerFilter;
import org.inspirenxe.skills.api.skill.builtin.query.EventQuery;
import org.inspirenxe.skills.impl.skill.builtin.query.EventQueryImpl;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.EventContext;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractBulkTransactionEventProcessor<T extends DataSerializable> extends AbstractEventProcessor {

    AbstractBulkTransactionEventProcessor(final String id, final String name, final Predicate<Event> shouldProcess) {
        super(id, name, shouldProcess);
    }

    @Override
    public void process(final Event event, final EventContext context, final SkillService service, final User user, final Skill skill) {
        final BasicSkillType skillType = (BasicSkillType) skill.getSkillType();

        final List<FilterRegistrar> filterRegistrations = skillType.getFilterRegistrations(skill.getHolder().getContainer(), this);

        final EventContext actualContext = this.populateEventContext(event, context, service);

        EventQuery query = new EventQueryImpl(event.getCause(), actualContext, user, skill);

        for (final FilterRegistrar registration : filterRegistrations) {
            if (event instanceof Cancellable) {
                for (final Filter filter : registration.getFilters(CANCEL_EVENT)) {
                    if (filter.query(query) == DENY) {
                        ((Cancellable) event).setCancelled(true);
                        return;
                    }
                }
            }

            for (final TriggerFilter trigger : registration.getTriggers(EVENT)) {
                this.processTrigger(trigger, query);
            }

            List<Transaction<T>> transactions = this.getTransactions(event);

            for (final Transaction<T> transaction : transactions) {

                query = new EventQueryImpl(event.getCause(), this.populateTransactionContext(event, actualContext, transaction), user, skill);

                for (final Filter filter : registration.getFilters(CANCEL_TRANSACTION)) {
                    if (filter.query(query) == DENY) {
                        transaction.setValid(false);
                        break;
                    }
                }
            }

            transactions = transactions
                .stream()
                .filter(Transaction::isValid)
                .collect(Collectors.toList());

            for (Transaction<T> transaction : transactions) {
                query = new EventQueryImpl(event.getCause(), this.populateTransactionContext(event, actualContext, transaction), user, skill);

                for (final TriggerFilter trigger : registration.getTriggers(TRANSACTION)) {
                    this.processTrigger(trigger, query);
                }
            }
        }
    }

    @Override
    EventContext populateEventContext(final Event event, final EventContext context, final SkillService service) {
        return EventContext.builder()
            .from(context)
            .add(PROCESSING_PLAYER, Objects.requireNonNull(event.getCause().first(Player.class).orElse(null)))
            .add(BLOCK_CREATION_TRACKER, service.getBlockCreationTracker())
            .build();
    }

    abstract List<Transaction<T>> getTransactions(Event event);

    abstract EventContext populateTransactionContext(Event event, EventContext context, Transaction<T> transaction);
}
