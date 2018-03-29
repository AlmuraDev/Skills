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
import org.inspirenxe.skills.impl.content.type.effect.AbstractContentEffectTypeBuilder;
import org.inspirenxe.skills.impl.effect.firework.FireworkEffectTypeImpl;
import org.inspirenxe.skills.impl.effect.firework.SkillsFireworkEffectType;
import org.spongepowered.api.item.FireworkEffect;
import org.spongepowered.api.item.FireworkShape;

public final class ContentFireworkEffectTypeBuilderImpl extends AbstractContentEffectTypeBuilder<SkillsFireworkEffectType>
    implements ContentFireworkEffectTypeBuilder {

  private RegistryReference<FireworkShape> shape;

  @Override
  public void shape(RegistryReference<FireworkShape> shape) {
    this.shape = shape;
  }

  @Override
  public SkillsFireworkEffectType build() {
    checkNotNull(this.key());

    final FireworkEffect effect = FireworkEffect.builder()
        .shape(this.shape.require())
        .build();

    return new FireworkEffectTypeImpl(this.key(), effect);
  }
}
