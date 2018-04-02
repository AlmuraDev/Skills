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
package org.inspirenxe.skills.impl.content.type.effect.sound;


import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.droplet.registry.reference.RegistryReference;
import org.inspirenxe.skills.api.sound.SoundEffect;
import org.inspirenxe.skills.impl.content.type.effect.AbstractContentEffectTypeBuilder;
import org.inspirenxe.skills.impl.effect.sound.SkillsSoundEffectType;
import org.inspirenxe.skills.impl.effect.sound.SoundEffectTypeImpl;
import org.spongepowered.api.effect.sound.SoundCategory;
import org.spongepowered.api.effect.sound.SoundType;

public final class ContentSoundEffectTypeBuilderImpl extends AbstractContentEffectTypeBuilder<SkillsSoundEffectType>
    implements ContentSoundEffectTypeBuilder {

  private RegistryReference<SoundType> sound;
  private RegistryReference<SoundCategory> category;
  private double minVolume, volume, pitch;

  @Override
  public void sound(RegistryReference<SoundType> sound) {
    this.sound = sound;
  }

  @Override
  public void category(RegistryReference<SoundCategory> category) {
    this.category = category;
  }

  @Override
  public void minVolume(double minVolume) {
    this.minVolume = minVolume;
  }

  @Override
  public void volume(double volume) {
    this.volume = volume;
  }

  @Override
  public void pitch(double pitch) {
    this.pitch = pitch;
  }

  @Override
  public SkillsSoundEffectType build() {
    checkNotNull(this.sound);
    checkNotNull(this.category);

    final SoundEffect effect = SoundEffect.builder()
        .soundType(this.sound.require())
        .soundCategory(this.category.require())
        .minVolume(this.minVolume)
        .volume(this.volume)
        .pitch(this.pitch)
        .build();

    return new SoundEffectTypeImpl(this.key(), effect);
  }
}
