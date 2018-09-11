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
package org.inspirenxe.skills.impl.sound;

import com.google.common.base.MoreObjects;
import org.inspirenxe.skills.api.sound.SoundEffect;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;

public final class SoundEffectImpl implements SoundEffect {

  private final SoundType type;
  private final SoundCategory category;
  private final double minVolume, volume, pitch;

  SoundEffectImpl(final SoundType type, final SoundCategory category, final double minVolume, final double volume, final double pitch) {
    this.type = type;
    this.category = category;
    this.minVolume = minVolume;
    this.volume = volume;
    this.pitch = pitch;
  }

  @Override
  public SoundType getType() {
    return this.type;
  }

  @Override
  public SoundCategory getCategory() {
    return this.category;
  }

  @Override
  public double getMinVolume() {
    return this.minVolume;
  }

  @Override
  public double getVolume() {
    return this.volume;
  }

  @Override
  public double getPitch() {
    return this.pitch;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("type", this.type)
        .add("category", this.category)
        .add("minVolume", this.minVolume)
        .add("volume", this.volume)
        .add("pitch", this.pitch)
        .toString();
  }

  @Override
  public int getContentVersion() {
    return 1;
  }

  @Override
  public DataContainer toContainer() {
    // TODO Need to ask gabizou on this
    return new MemoryDataContainer();
  }
}
