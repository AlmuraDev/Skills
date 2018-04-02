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

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.function.Predicate;

public interface LazyItemStack extends Predicate<ItemStack> {

  @Deprecated // 1.13
      int DEFAULT_DATA = 0;
  int DEFAULT_QUANTITY = 1;

  ItemType item();

  @Deprecated // 1.13
  int data();

  int quantity();

  default ItemStack stack() {
    return ItemStack.builder()
        .itemType(this.item())
        .quantity(this.quantity())
        .add(Keys.ITEM_DURABILITY, this.data())
        .build();
  }

  @Override
  default boolean test(final ItemStack item) {
    return item.getType() == this.item() && item.get(Keys.ITEM_DURABILITY).orElse(null) == this.data();
  }

  default boolean matches(final ItemType that) {
    return this.item() == that;
  }

  default boolean matches(final LazyItemStack that) {
    return this.item() == that.item() && this.data() == that.data();
  }
}
