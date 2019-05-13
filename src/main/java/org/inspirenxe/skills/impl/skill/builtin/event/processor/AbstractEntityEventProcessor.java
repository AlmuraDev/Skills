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
import static org.inspirenxe.skills.api.skill.builtin.RegistrarTypes.CANCEL_ENTITY_SPAWN;
import static org.inspirenxe.skills.api.skill.builtin.RegistrarTypes.CANCEL_EVENT;
import static org.inspirenxe.skills.api.skill.builtin.TriggerRegistrarTypes.ENTITY_SPAWN;
import static org.inspirenxe.skills.api.skill.builtin.TriggerRegistrarTypes.EVENT;

import net.kyori.filter.Filter;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.builtin.BasicSkillType;
import org.inspirenxe.skills.api.skill.builtin.FilterRegistrar;
import org.inspirenxe.skills.api.skill.builtin.SkillsEventContextKeys;
import org.inspirenxe.skills.api.skill.builtin.filter.applicator.TriggerFilter;
import org.inspirenxe.skills.api.skill.builtin.query.EventQuery;
import org.inspirenxe.skills.impl.skill.builtin.query.EventQueryImpl;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractEntityEventProcessor extends AbstractEventProcessor {

    AbstractEntityEventProcessor(final String id, final String name, final Predicate<Event> shouldProcess) {
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

            List<Entity> entities = new ArrayList<>(this.getEntities(event));

            for (final Entity entity : entities) {

                query = new EventQueryImpl(event.getCause(), this.populateEntityContext(context, entity), user, skill);

                for (final Filter filter : registration.getFilters(CANCEL_ENTITY_SPAWN)) {
                    if (filter.query(query) == DENY) {
                        this.cancelEntity(event, entity);
                        entities.removeIf(e -> e == entity);
                        break;
                    }
                }
            }

            for (final Entity entity : entities) {
                query = new EventQueryImpl(event.getCause(), this.populateEntityContext(context, entity), user, skill);

                for (final TriggerFilter trigger : registration.getTriggers(ENTITY_SPAWN)) {
                    this.processTrigger(trigger, query);
                }
            }
        }
    }

    @Override
    public EventContext populateEventContext(final Event event, final EventContext context, final SkillService service) {
        return context;
    }

    abstract List<Entity> getEntities(Event event);

    EventContext populateEntityContext(final EventContext context, final Entity entity) {
        return EventContext
            .builder()
            .from(context)
            .add(SkillsEventContextKeys.PROCESSING_ITEM, context.get(EventContextKeys.USED_ITEM).orElse(ItemStackSnapshot.NONE))
            .add(SkillsEventContextKeys.PROCESSING_ENTITY, entity)
            .build();
    }

    abstract void cancelEntity(Event event, Entity entity);
}
