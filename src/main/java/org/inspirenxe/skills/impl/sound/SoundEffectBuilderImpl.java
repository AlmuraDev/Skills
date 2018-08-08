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

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.api.sound.SoundEffect;
import org.spongepowered.api.effect.sound.SoundCategories;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;

import javax.annotation.Nullable;

// TODO This needs to go to common..
public final class SoundEffectBuilderImpl implements SoundEffect.Builder {

  @Nullable private SoundType soundType;
  @Nullable private SoundCategory soundCategory;
  private double minVolume, volume, pitch;

  @Override
  public SoundEffect.Builder soundType(final SoundType soundType) {
    this.soundType = soundType;
    return this;
  }

  @Override
  public SoundEffect.Builder soundCategory(final SoundCategory soundCategory) {
    this.soundCategory = soundCategory;
    return this;
  }

  @Override
  public SoundEffect.Builder minVolume(final double minVolume) {
    this.minVolume = minVolume;
    return this;
  }

  @Override
  public SoundEffect.Builder volume(final double volume) {
    this.volume = volume;
    return this;
  }

  @Override
  public SoundEffect.Builder pitch(final double pitch) {
    this.pitch = pitch;
    return this;
  }

  @Override
  public SoundEffect.Builder from(final SoundEffect value) {
    this.soundType = value.getType();
    this.soundCategory = value.getCategory();
    this.minVolume = value.getMinVolume();
    this.volume = value.getVolume();
    this.pitch = value.getPitch();
    return this;
  }

  @Override
  public SoundEffect.Builder reset() {
    this.soundType = null;
    this.soundCategory = SoundCategories.MASTER;
    this.minVolume = 0;
    this.volume = 1;
    this.pitch = 0;
    return this;
  }

  @Override
  public SoundEffect build() {
    checkNotNull(this.soundType);
    checkNotNull(this.soundCategory);

    return new SoundEffectImpl(this.soundType, this.soundCategory, this.minVolume, this.volume, this.pitch);
  }
}
