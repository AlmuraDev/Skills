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
import static org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys.PROCESSING_PLAYER;
import static org.inspirenxe.skills.api.skill.builtin.TriggerRegistrarTypes.EVENT;

import net.kyori.filter.Filter;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.builtin.BasicSkillType;
import org.inspirenxe.skills.api.skill.builtin.FilterRegistrar;
import org.inspirenxe.skills.api.skill.builtin.filter.trigger.TriggerFilter;
import org.inspirenxe.skills.api.skill.builtin.query.EventQuery;
import org.inspirenxe.skills.impl.skill.builtin.query.EventQueryImpl;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;


public abstract class AbstractBulkItemEventProcessor extends AbstractEventProcessor {

    AbstractBulkItemEventProcessor(String id, String name, Predicate<Event> shouldProcess) {
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

            final List<ItemStackSnapshot> snapshots = new ArrayList<>(this.getItemStacks(event));

            for (final ItemStackSnapshot snapshot : snapshots) {
                query = new EventQueryImpl(event.getCause(), this.populateItemStackContext(event, context, snapshot), user, skill);

                for (final TriggerFilter trigger : registration.getTriggers(EVENT)) {
                    this.processTrigger(trigger, query);
                }
            }
        }
    }

    abstract List<ItemStackSnapshot> getItemStacks(Event event);

    @Override
    EventContext populateEventContext(Event event, EventContext context, SkillService service) {
        return EventContext
            .builder()
            .from(context)
            .add(PROCESSING_PLAYER, Objects.requireNonNull(event.getCause().first(Player.class).orElse(null)))
            .build();
    }

    abstract EventContext populateItemStackContext(Event event, EventContext context, ItemStackSnapshot snapshot);
}
