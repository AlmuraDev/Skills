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
package org.inspirenxe.skills.impl.skill.builtin.registry.module;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.api.skill.builtin.TriggerRegistrarType;
import org.inspirenxe.skills.api.skill.builtin.TriggerRegistrarTypes;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.skill.builtin.TriggerRegistrarTypeImpl;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.DelayedRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Singleton;

@Singleton
public final class TriggerRegistrarTypeRegistryModule implements AdditionalCatalogRegistryModule<TriggerRegistrarType>,
    AlternateCatalogRegistryModule<TriggerRegistrarType> {

    @RegisterCatalog(TriggerRegistrarTypes.class)
    private final Map<String, TriggerRegistrarType> map = new HashMap<>();

    @Override
    public Optional<TriggerRegistrarType> getById(final String id) {
        return Optional.ofNullable(this.map.get(checkNotNull(id)));
    }

    @Override
    public Collection<TriggerRegistrarType> getAll() {
        return Collections.unmodifiableCollection(this.map.values());
    }

    @Override
    public void registerAdditionalCatalog(final TriggerRegistrarType catalogType) {
        this.map.put(catalogType.getId(), catalogType);
    }

    @DelayedRegistration(RegistrationPhase.PRE_INIT)
    @Override
    public void registerDefaults() {
        this.registerAdditionalCatalog(new TriggerRegistrarTypeImpl(SkillsImpl.ID + ":event", "Event"));
        this.registerAdditionalCatalog(new TriggerRegistrarTypeImpl(SkillsImpl.ID + ":transaction", "Transaction"));
        this.registerAdditionalCatalog(new TriggerRegistrarTypeImpl(SkillsImpl.ID + ":entity", "Entity"));
    }

    @Override
    public Map<String, TriggerRegistrarType> provideCatalogMap() {
        final Map<String, TriggerRegistrarType> fixedMap = new HashMap<>();

        for (Map.Entry<String, TriggerRegistrarType> entry : this.map.entrySet()) {
            fixedMap.put(entry.getKey().replace("skills:", ""), entry.getValue());
        }

        return fixedMap;
    }
}
