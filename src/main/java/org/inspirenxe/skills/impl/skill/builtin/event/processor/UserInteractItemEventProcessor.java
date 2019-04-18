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
import org.inspirenxe.skills.api.skill.builtin.query.EventQuery;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.skill.builtin.query.ItemStackQuery;
import org.inspirenxe.skills.impl.skill.builtin.query.PlayerItemStackQuery;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;

import java.util.List;
import java.util.function.Predicate;

public final class UserInteractItemEventProcessor extends AbstractEventProcessor {

    public UserInteractItemEventProcessor(final String id, final String name, final Predicate<Event> shouldProcess) {
        super(SkillsImpl.ID + ":" + id, name, shouldProcess);
    }

    @Override
    public void process(final Event event, final SkillService service, final Skill skill) {
        final User user = event.getCause().first(User.class).orElse(null);
        if (user == null) {
            return;
        }

        final InteractItemEvent actualEvent = (InteractItemEvent) event;

        final BasicSkillType skillType = (BasicSkillType) skill.getSkillType();

        final List<FilterRegistrar> filterRegistrations = skillType.getFilterRegistrations(skill.getHolder().getContainer(), this);

        EventQuery query;

        for (final FilterRegistrar registration : filterRegistrations) {
            if (registration.getCancelEvent() != null) {

                if (user.getPlayer().isPresent()) {
                    query = new PlayerItemStackQuery(event.getCause(), event.getContext(), skill, user.getPlayer().get(), actualEvent.getItemStack());
                } else {
                    query = new ItemStackQuery(event.getCause(), event.getContext(), skill, actualEvent.getItemStack());
                }

                if (registration.getCancelEvent().query(query) == FilterResponse.DENY) {
                    actualEvent.setCancelled(true);
                    break;
                }
            }

            // TODO Event Triggers
        }
    }
}
