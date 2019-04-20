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

import org.inspirenxe.skills.api.skill.builtin.EventProcessor;
import org.inspirenxe.skills.api.skill.builtin.EventProcessors;
import org.inspirenxe.skills.impl.skill.builtin.event.processor.ChangeBlockPlaceEventProcessor;
import org.inspirenxe.skills.impl.skill.builtin.event.processor.InteractBlockEventProcessor;
import org.inspirenxe.skills.impl.skill.builtin.event.processor.InteractItemEventProcessor;
import org.inspirenxe.skills.impl.skill.builtin.event.processor.ChangeBlockBreakEventProcessor;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
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
public final class EventProcessorRegistryModule implements AdditionalCatalogRegistryModule<EventProcessor>, AlternateCatalogRegistryModule<EventProcessor> {

    @RegisterCatalog(EventProcessors.class)
    private final Map<String, EventProcessor> map = new HashMap<>();

    @Override
    public Optional<EventProcessor> getById(final String id) {
        return Optional.ofNullable(this.map.get(checkNotNull(id)));
    }

    @Override
    public Collection<EventProcessor> getAll() {
        return Collections.unmodifiableCollection(this.map.values());
    }

    @Override
    public void registerAdditionalCatalog(final EventProcessor catalogType) {
        this.map.put(catalogType.getId(), catalogType);
    }

    @DelayedRegistration(RegistrationPhase.PRE_INIT)
    @Override
    public void registerDefaults() {
        this.registerAdditionalCatalog(new InteractItemEventProcessor("interact_item_primary_main_hand", "Interact Item Primary Main Hand", e -> e instanceof InteractItemEvent.Primary.MainHand));
        this.registerAdditionalCatalog(new InteractItemEventProcessor("interact_item_primary_off_hand", "Interact Item Primary Off Hand", e -> e instanceof InteractItemEvent.Primary.OffHand));
        this.registerAdditionalCatalog(new InteractItemEventProcessor("interact_item_secondary_main_hand", "Interact Item Secondary Main Hand", e -> e instanceof InteractItemEvent.Secondary.MainHand));
        this.registerAdditionalCatalog(new InteractItemEventProcessor("interact_item_secondary_off_hand", "Interact Item Secondary Off Hand", e -> e instanceof InteractItemEvent.Secondary.OffHand));
        this.registerAdditionalCatalog(new InteractBlockEventProcessor("interact_block_primary_main_hand", "Interact Block Primary Main Hand", e -> e instanceof InteractBlockEvent.Primary.MainHand));
        this.registerAdditionalCatalog(new InteractBlockEventProcessor("interact_block_primary_off_hand", "Interact Block Primary Off Hand", e -> e instanceof InteractBlockEvent.Primary.OffHand));
        this.registerAdditionalCatalog(new InteractBlockEventProcessor("interact_block_secondary_main_hand", "Interact Block Secondary Main Hand", e -> e instanceof InteractBlockEvent.Secondary.MainHand));
        this.registerAdditionalCatalog(new InteractBlockEventProcessor("interact_block_secondary_off_hand", "Interact Block Secondary Off Hand", e -> e instanceof InteractBlockEvent.Secondary.OffHand));
        this.registerAdditionalCatalog(new ChangeBlockPlaceEventProcessor());
        this.registerAdditionalCatalog(new ChangeBlockBreakEventProcessor());
    }

    @Override
    public Map<String, EventProcessor> provideCatalogMap() {
        final Map<String, EventProcessor> fixedMap = new HashMap<>();

        for (Map.Entry<String, EventProcessor> entry : this.map.entrySet()) {
            fixedMap.put(entry.getKey().replace("skills:", ""), entry.getValue());
        }

        return fixedMap;
    }
}
