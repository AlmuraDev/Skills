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

import net.kyori.filter.FilterResponse;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.builtin.BasicSkillType;
import org.inspirenxe.skills.api.skill.builtin.FilterRegistrar;
import org.inspirenxe.skills.api.skill.builtin.applicator.Applicator;
import org.inspirenxe.skills.api.skill.builtin.filter.applicator.ApplicatorEntry;
import org.inspirenxe.skills.api.skill.builtin.filter.applicator.TriggerFilter;
import org.inspirenxe.skills.api.skill.builtin.query.EventQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractTransactionEventProcessor<T extends DataSerializable> extends AbstractEventProcessor {

    AbstractTransactionEventProcessor(final String id, final String name, final Predicate<Event> shouldProcess) {
        super(id, name, shouldProcess);
    }

    @Override
    public void process(final Event event, final SkillService service, final Skill skill) {
        final User user = event.getCause().first(User.class).orElse(null);
        if (user == null) {
            return;
        }

        final BasicSkillType skillType = (BasicSkillType) skill.getSkillType();

        final List<FilterRegistrar> filterRegistrations = skillType.getFilterRegistrations(skill.getHolder().getContainer(), this);

        EventQuery query;

        for (final FilterRegistrar registration : filterRegistrations) {
            // TODO Event Triggers
            if (event instanceof Cancellable && registration.getCancelEvent() != null) {

                query = this.getCancelEventQuery(event, user, skill);

                if (registration.getCancelEvent().query(query) == FilterResponse.DENY) {
                    ((Cancellable) event).setCancelled(true);
                    break;
                }
            }

            if (registration.getCancelTransaction() != null || !registration.getTransactionTriggers().isEmpty()) {
                for (final Transaction<T> transaction : this.getTransactions(event)) {

                    query = this.getCancelTransactionQuery(event, user, service, skill, transaction);

                    if (registration.getCancelTransaction() != null) {
                        if (registration.getCancelTransaction().query(query) == FilterResponse.DENY) {
                            transaction.setValid(false);
                            continue;
                        }

                        for (final TriggerFilter trigger : registration.getTransactionTriggers()) {

                            boolean runFallbackApplicators = trigger.getElseApplicators() != null;

                            if (trigger.query(query) == FilterResponse.ALLOW) {
                                final Iterable<ApplicatorEntry> applicatorEntries = trigger.getMatchedApplicators(query);
                                for (final ApplicatorEntry applicatorEntry : applicatorEntries) {
                                    runFallbackApplicators = false;
                                    for (final Applicator applicator : applicatorEntry.getApplicators()) {
                                        applicator.apply(query);
                                    }
                                }

                                if (runFallbackApplicators) {
                                    for (final Applicator applicator : trigger.getElseApplicators()) {
                                        applicator.apply(query);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public abstract EventQuery getCancelEventQuery(Event event, User user, Skill skill);

    public abstract List<Transaction<T>> getTransactions(Event event);

    public abstract EventQuery getCancelTransactionQuery(Event event, User user, SkillService service, Skill skill, Transaction<T> transaction);
}
