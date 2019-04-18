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
package org.inspirenxe.skills.impl.skill.builtin.query;

import net.kyori.filter.Filter;
import org.inspirenxe.skills.api.skill.Skill;
import org.inspirenxe.skills.api.skill.builtin.BasicSkillType;
import org.inspirenxe.skills.api.skill.builtin.filter.level.LevelFilter;
import org.inspirenxe.skills.api.skill.builtin.query.EventQuery;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.text.Text;

public class EventQueryImpl implements EventQuery {

    private final Cause cause;
    private final EventContext context;
    private final Skill skill;

    public EventQueryImpl(final Cause cause, final EventContext context, final Skill skill) {
        this.cause = cause;
        this.context = context;
        this.skill = skill;
    }

    @Override
    public final Cause getCause() {
        return this.cause;
    }

    @Override
    public final EventContext getContext() {
        return this.context;
    }

    @Override
    public final Skill getSkill() {
        return this.skill;
    }

    @Override
    public void denied(final Filter filter) {
        if (filter instanceof LevelFilter) {
            if (!(this.skill.getSkillType() instanceof BasicSkillType)) {
                return;
            }
            final BasicSkillType skillType = (BasicSkillType) this.skill.getSkillType();
            final Player player = this.cause.first(Player.class).orElse(null);
            if (player != null) {

                if (player.hasPermission(SkillsImpl.ID + ".notification.deny." + skill.getSkillType().getName()
                    .toLowerCase(Sponge.getServer().getConsole().getLocale()))) {
                    player.sendMessage(Text.of("You require ", skillType.getFormattedName(), " level ", ((LevelFilter) filter).getLevel() + " to "
                        + "perform this action."));
                }
            }
        }
    }
}
