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
package org.inspirenxe.skills.impl.parser.lazy.item;

import com.almuradev.droplet.registry.reference.RegistryReference;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

public final class LazyItemStackImpl implements LazyItemStack {

  private final RegistryReference<ItemType> item;
  @Deprecated private final int data;
  private final int quantity;

  LazyItemStackImpl(final RegistryReference<ItemType> item, @Deprecated final int data, final int quantity) {
    this.item = item;
    this.data = data;
    this.quantity = quantity;
  }

  @Override
  public ItemType item() {
    return this.item.require();
  }

  @Deprecated
  @Override
  public int data() {
    return this.data;
  }

  @Override
  public int quantity() {
    return this.quantity;
  }

  @Override
  public ItemStack get() {
    return this.stack();
  }
}
