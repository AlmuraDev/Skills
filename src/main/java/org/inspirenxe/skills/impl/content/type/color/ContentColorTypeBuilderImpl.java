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
package org.inspirenxe.skills.impl.content.type.color;

import com.almuradev.droplet.content.type.AbstractContentBuilder;
import org.inspirenxe.skills.impl.color.ColorTypeImpl;
import org.spongepowered.api.util.Color;

public final class ContentColorTypeBuilderImpl extends AbstractContentBuilder<ColorTypeImpl> implements ContentColorTypeBuilder {

  private static final int NOT_SET = -1;

  private int b = NOT_SET;
  private int g = NOT_SET;
  private int hex = NOT_SET;
  private int r = NOT_SET;

  @Override
  public void b(int b) {
    this.b = b;
  }

  @Override
  public void g(int g) {
    this.g = g;
  }

  @Override
  public void hex(int hex) {
    this.hex = hex;
  }

  @Override
  public void r(int r) {
    this.r = r;
  }

  @Override
  public ColorTypeImpl build() {
    final Color color;

    if (this.hex > NOT_SET) {
      color = Color.ofRgb(this.hex);
    } else {
      if (this.b <= NOT_SET || this.g <= NOT_SET || this.r <= NOT_SET) {
        throw new IllegalStateException("Must provide a value for each component of a color!");
      }

      color = Color.ofRgb(this.r, this.g, this.b);
    }

    return new ColorTypeImpl(this.key(), color);
  }
}
