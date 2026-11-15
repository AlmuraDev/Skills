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
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.api.Skills;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;

import java.util.UUID;

/**
 * Keeps a container per loaded world and a holder per online player in the container of their world: loaded from the database when
 * they join or arrive, saved when they leave.
 */
public final class HolderListener {

    @Listener(order = Order.PRE)
    public void onLoadWorld(final LoadWorldEvent event) {
        if (Skills.isAvailable()) {
            Skills.service().createContainer(event.world().key());
        }
    }

    @Listener(order = Order.PRE)
    public void onUnloadWorld(final UnloadWorldEvent event) {
        if (Skills.isAvailable()) {
            Skills.service().removeContainer(event.world().key()).ifPresent(SkillHolderContainer::save);
        }
    }

    @Listener(order = Order.LAST)
    public void onJoin(final ServerSideConnectionEvent.Join event) {
        final ServerPlayer player = event.player();
        if (Skills.isAvailable()) {
            Skills.service().containerFor(player.world().key()).ifPresent(container -> HolderListener.load(container, player));
        }
    }

    @Listener(order = Order.LAST)
    public void onChangeWorld(final ChangeEntityWorldEvent.Post event) {
        if (event.entity() instanceof ServerPlayer player) {
            this.changeContainer(event.originalWorld().key(), event.destinationWorld().key(), player);
        }
    }

    @Listener(order = Order.LAST)
    public void onRespawn(final RespawnPlayerEvent.Post event) {
        this.changeContainer(event.originalWorld().key(), event.destinationWorld().key(), event.entity());
    }

    @Listener(order = Order.PRE)
    public void onLeave(final ServerSideConnectionEvent.Leave event) {
        final ServerPlayer player = event.player();
        if (!Skills.isAvailable()) {
            return;
        }
        // Experience is snapshotted here and written on the database thread (and waited for while the server stops)
        try (CauseStackManager.StackFrame frame = Sponge.server().causeStackManager().pushCauseFrame()) {
            Skills.service().containerFor(player.world().key())
                    .flatMap(container -> container.removeHolder(player.uniqueId()))
                    .ifPresent(SkillHolder::save);
        }
    }

    /**
     * Loads the holders of the players already online, e.g. after Skills enabled late.
     */
    public static void loadOnline() {
        for (final ServerPlayer player : Sponge.server().onlinePlayers()) {
            Skills.service().containerFor(player.world().key()).ifPresent(container -> HolderListener.load(container, player));
        }
    }

    private static void load(final SkillHolderContainer container, final ServerPlayer player) {
        // Keep a live holder as is, reloading it would discard unsaved experience
        if (container.holder(player.uniqueId()).isPresent()) {
            return;
        }
        container.createHolder(player.uniqueId(), player.name()).skills().values().forEach(Skill::load);
    }

    private void changeContainer(final ResourceKey from, final ResourceKey to, final ServerPlayer player) {
        if (!Skills.isAvailable() || from.equals(to)) {
            return;
        }
        final SkillService service = Skills.service();
        final SkillHolderContainer fromContainer = service.containerFor(from).orElse(null);
        final SkillHolderContainer toContainer = service.containerFor(to).orElse(null);
        // Worlds share the same container (e.g. Overworld <-> Nether), keep the live holder
        if (fromContainer == toContainer) {
            return;
        }
        final UUID playerId = player.uniqueId();
        // Saves and loads run in submission order on the database thread, so the load below always sees this save
        if (fromContainer != null) {
            try (CauseStackManager.StackFrame frame = Sponge.server().causeStackManager().pushCauseFrame()) {
                fromContainer.removeHolder(playerId).ifPresent(SkillHolder::save);
            }
        }
        if (toContainer != null) {
            HolderListener.load(toContainer, player);
        }
    }
}
