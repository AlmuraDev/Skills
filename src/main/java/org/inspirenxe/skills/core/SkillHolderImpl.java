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

import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillHolderContainer;
import org.inspirenxe.skills.api.SkillType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class SkillHolderImpl implements SkillHolder {

    private final SkillHolderContainer container;
    private final UUID uniqueId;
    private final String name;
    private final Map<SkillType, Skill> skills = new LinkedHashMap<>();

    SkillHolderImpl(final SkillHolderContainer container, final UUID uniqueId, final String name) {
        this.container = container;
        this.uniqueId = uniqueId;
        this.name = name;
    }

    void addSkill(final Skill skill) {
        this.skills.put(skill.type(), skill);
    }

    @Override
    public UUID uniqueId() {
        return this.uniqueId;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public SkillHolderContainer container() {
        return this.container;
    }

    @Override
    public Map<SkillType, Skill> skills() {
        return Collections.unmodifiableMap(this.skills);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof SkillHolderImpl other && this.uniqueId.equals(other.uniqueId)
                && this.container.key().equals(other.container.key());
    }

    @Override
    public int hashCode() {
        return this.uniqueId.hashCode();
    }

    @Override
    public String toString() {
        return "SkillHolder{" + this.name + " (" + this.uniqueId + "), container=" + this.container.key() + "}";
    }
}
