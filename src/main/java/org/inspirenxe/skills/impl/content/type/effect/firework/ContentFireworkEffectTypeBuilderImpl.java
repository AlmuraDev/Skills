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
package org.inspirenxe.skills.impl.content.type.effect.firework;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.droplet.registry.reference.RegistryReference;
import org.inspirenxe.skills.api.color.ColorType;
import org.inspirenxe.skills.impl.content.type.effect.AbstractContentEffectTypeBuilder;
import org.inspirenxe.skills.impl.effect.firework.FireworkEffectTypeImpl;
import org.inspirenxe.skills.impl.effect.firework.SkillsFireworkEffectType;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class ContentFireworkEffectTypeBuilderImpl extends AbstractContentEffectTypeBuilder<SkillsFireworkEffectType>
    implements ContentFireworkEffectTypeBuilder {

  @Nullable private RegistryReference<FireworkShape> shape;
  @Nullable private List<RegistryReference<ColorType>> colors;
  @Nullable private List<RegistryReference<ColorType>> fadeColors;
  private boolean flickers;
  private boolean trails;

  @Override
  public void shape(RegistryReference<FireworkShape> shape) {
    this.shape = shape;
  }

  @Override
  public void colors(List<RegistryReference<ColorType>> colors) {
    this.colors = colors;
  }

  @Override
  public void fadeColors(List<RegistryReference<ColorType>> fadeColors) {
    this.fadeColors = fadeColors;
  }

  @Override
  public void flickers(boolean flickers) {
    this.flickers = flickers;
  }

  @Override
  public void trails(boolean trails) {
    this.trails = trails;
  }

  @Override
  public SkillsFireworkEffectType build() {
    checkNotNull(this.shape);
    checkNotNull(this.colors);

    final FireworkEffect.Builder builder = FireworkEffect.builder()
        .shape(this.shape.require())
        .colors(this.colors.stream().map(RegistryReference::require).map(ColorType::getColor).collect(Collectors.toList()));
    if (this.fadeColors != null) {
      builder.fades(this.fadeColors.stream().map(RegistryReference::require).map(ColorType::getColor).collect(Collectors.toList()));
    }
    builder
        .flicker(this.flickers)
        .trail(this.trails);

    return new FireworkEffectTypeImpl(this.key(), builder.build());
  }
}
