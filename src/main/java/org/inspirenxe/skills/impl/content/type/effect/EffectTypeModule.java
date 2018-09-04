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
package org.inspirenxe.skills.impl.content.type.effect;

import com.almuradev.droplet.content.inject.ChildModule;
import com.almuradev.droplet.content.inject.ForChild;
import com.almuradev.droplet.content.inject.ForRoot;
import com.almuradev.droplet.content.inject.RootModule;
import com.almuradev.droplet.content.processor.Processor;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.inspirenxe.skills.impl.content.type.effect.firework.FireworkEffectTypeModule;
import org.inspirenxe.skills.impl.content.type.effect.potion.PotionEffectTypeModule;
import org.inspirenxe.skills.impl.content.type.effect.sound.SoundEffectTypeModule;

public final class EffectTypeModule extends RootModule.Impl<ContentEffectType.Child, ContentEffectTypeBuilder<?, ?>> {

  @Override
  protected void configure0() {
    this.bindRootType(new ContentEffectType.Root());
    this.bindRootLoader(new TypeLiteral<EffectTypeRootLoader>() {
    });

    this.inSet(Key.get(new TypeLiteral<Processor<? extends ContentEffectTypeBuilder<?, ?>>>() {
    }, ForRoot.class));

    this.installChild(new FireworkEffectTypeModule(), new BaseChildModule());
    this.installChild(new PotionEffectTypeModule(), new BaseChildModule());
    this.installChild(new SoundEffectTypeModule(), new BaseChildModule());
  }

  private static class BaseChildModule extends ChildModule.Impl<ContentEffectType.Child> {

    @Override
    protected void configure0() {
      this.inSet(Key.get(Processor.class, ForChild.class));
    }
  }
}
