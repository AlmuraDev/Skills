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
package org.inspirenxe.skills.impl.content.type.effect.potion;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.impl.content.type.effect.AbstractContentEffectTypeBuilder;
import org.inspirenxe.skills.impl.effect.potion.PotionEffectTypeImpl;
import org.inspirenxe.skills.impl.effect.potion.SkillsPotionEffectType;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectType;

import javax.annotation.Nullable;

public final class ContentPotionEffectTypeBuilderImpl extends AbstractContentEffectTypeBuilder<SkillsPotionEffectType>
    implements ContentPotionEffectTypeBuilder {

  @Nullable private PotionEffectType potion;
  private int duration, amplifier;
  private boolean isAmbient, showParticles;

  @Override
  public void potion(final PotionEffectType potion) {
    this.potion = potion;
  }

  @Override
  public void duration(final int duration) {
    this.duration = duration;
  }

  @Override
  public void amplifier(final int amplifier) {
    this.amplifier = amplifier;
  }

  @Override
  public void isAmbient(final boolean isAmbient) {
    this.isAmbient = isAmbient;
  }

  @Override
  public void showParticles(final boolean showParticles) {
    this.showParticles = showParticles;
  }

  @Override
  public SkillsPotionEffectType build() {
    checkNotNull(this.potion);

    final PotionEffect effect = PotionEffect.builder()
        .potionType(this.potion)
        .duration(this.duration)
        .amplifier(this.amplifier)
        .ambience(this.isAmbient)
        .particles(this.showParticles)
        .build();

    return new PotionEffectTypeImpl(this.key(), effect);
  }
}
