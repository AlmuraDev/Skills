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
package org.inspirenxe.skills.impl.content.component.filter;

import net.kyori.fragment.filter.FilterBinder;
import net.kyori.violet.AbstractModule;
import org.inspirenxe.skills.impl.content.component.apply.data.KeyValue;
import org.inspirenxe.skills.impl.content.component.filter.block.BlockFilterParser;
import org.inspirenxe.skills.impl.content.component.filter.cause.CauseFilterParser;
import org.inspirenxe.skills.impl.content.component.filter.data.DataFilter;
import org.inspirenxe.skills.impl.content.component.filter.data.DataFilterParser;
import org.inspirenxe.skills.impl.content.component.filter.experience.ExperienceFilterParser;
import org.inspirenxe.skills.impl.content.component.filter.experience.LevelFilterParser;
import org.inspirenxe.skills.impl.content.component.filter.item.ItemFilterParser;
import org.inspirenxe.skills.impl.content.component.filter.key.NamespaceFilterParser;
import org.inspirenxe.skills.impl.content.component.filter.key.RegistryKeyFilterParser;
import org.inspirenxe.skills.impl.content.component.filter.notifier.NotifierFilterParser;
import org.inspirenxe.skills.impl.content.component.filter.owner.OwnerFilterParser;
import org.inspirenxe.skills.impl.content.component.filter.potion.PotionFilterParser;

public final class FilterModule extends AbstractModule {

  @Override
  protected void configure() {
    final TypedMultiFilterBinder filters = new TypedMultiFilterBinder(this.binder());
    filters.bindFilter("block").to(BlockFilterParser.class);
    filters.bindFilter("cause").to(CauseFilterParser.class);
    filters.bindFilter("data").to(DataFilterParser.class);
    filters.bindFilter("experience").to(ExperienceFilterParser.class);
    filters.bindFilter("item").to(ItemFilterParser.class);
    filters.bindFilter("key").to(RegistryKeyFilterParser.class);
    filters.bindFilter("level").to(LevelFilterParser.class);
    filters.bindFilter("owner").to(OwnerFilterParser.class);
    filters.bindFilter("namespace").to(NamespaceFilterParser.class);
    filters.bindFilter("notifier").to(NotifierFilterParser.class);
    filters.bindFilter("potion").to(PotionFilterParser.class);
  }
}
