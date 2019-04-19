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
import org.inspirenxe.skills.api.skill.builtin.EventProcessor;
import org.inspirenxe.skills.api.skill.builtin.FilterRegistrar;
import org.inspirenxe.skills.api.skill.builtin.applicator.Applicator;
import org.inspirenxe.skills.api.skill.builtin.filter.applicator.ApplicatorEntry;
import org.inspirenxe.skills.api.skill.builtin.filter.applicator.TriggerFilter;
import org.inspirenxe.skills.api.skill.builtin.query.EventQuery;
import org.inspirenxe.skills.impl.skill.builtin.query.EventQueryImpl;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.EventContext;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractEventProcessor implements EventProcessor {

    private final String id, name;
    private final Predicate<Event> shouldProcess;

    AbstractEventProcessor(final String id, final String name, final Predicate<Event> shouldProcess) {
        this.id = id;
        this.name = name;
        this.shouldProcess = shouldProcess;
    }

    @Override
    public final String getId() {
        return this.id;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final boolean shouldProcess(final Event event) {
        return this.shouldProcess.test(event);
    }

    @Override
    public void process(final Event event, final EventContext context, final SkillService service, final User user, final Skill skill) {
        final BasicSkillType skillType = (BasicSkillType) skill.getSkillType();
        final List<FilterRegistrar> filterRegistrations = skillType.getFilterRegistrations(skill.getHolder().getContainer(), this);
        final EventContext actualContext = this.populateEventContext(event, context, service);

        final EventQuery query = new EventQueryImpl(event.getCause(), actualContext, user, skill);
        for (final FilterRegistrar registration : filterRegistrations) {
            if (event instanceof Cancellable && registration.getCancelEvent() != null) {

                if (registration.getCancelEvent().query(query) == FilterResponse.DENY) {
                    ((Cancellable) event).setCancelled(true);
                    return;
                }
            }

            for (TriggerFilter trigger : registration.getEventTriggers()) {
                this.processTrigger(trigger, query);
            }
        }
    }

    abstract EventContext populateEventContext(Event event, EventContext context, SkillService service);

    final void processTrigger(final TriggerFilter trigger, final EventQuery query) {
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
