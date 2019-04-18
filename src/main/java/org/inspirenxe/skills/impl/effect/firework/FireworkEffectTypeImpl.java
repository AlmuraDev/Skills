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
package org.inspirenxe.skills.impl.effect.firework;

import com.almuradev.droplet.registry.RegistryKey;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class FireworkEffectTypeImpl implements SkillsFireworkEffectType {

    private final RegistryKey registryKey;
    private final FireworkEffect effect;

    public FireworkEffectTypeImpl(final RegistryKey registryKey, final FireworkEffect effect) {
        this.registryKey = registryKey;
        this.effect = effect;
    }

    @Override
    public String getId() {
        return this.registryKey.toString();
    }

    @Override
    public String getName() {
        return this.registryKey.value();
    }

    @Override
    public FireworkEffect getEffect() {
        return this.effect;
    }

    @Override
    public void play(final Location<World> location) {
        final Firework firework = (Firework) location.getExtent().createEntity(EntityTypes.FIREWORK, location.getPosition().add(0, 3, 0));
        firework.offer(firework.getFireworkData().addElement(this.effect));
        location.getExtent().spawnEntity(firework);
        firework.detonate();
    }
}
