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
package org.inspirenxe.skills.impl.component.filter;

import com.almuradev.droplet.component.filter.FilterBinder;
import net.kyori.violet.AbstractModule;
import org.inspirenxe.skills.impl.component.filter.block.BlockFilterParser;
import org.inspirenxe.skills.impl.component.filter.cause.CauseFilterParser;
import org.inspirenxe.skills.impl.component.filter.data.DataFilter;
import org.inspirenxe.skills.impl.component.filter.data.DataFilterParser;
import org.inspirenxe.skills.impl.component.filter.experience.ExperienceFilterParser;
import org.inspirenxe.skills.impl.component.filter.experience.LevelFilterParser;
import org.inspirenxe.skills.impl.component.filter.item.ItemFilterParser;
import org.inspirenxe.skills.impl.component.filter.key.NamespaceFilterParser;
import org.inspirenxe.skills.impl.component.filter.key.RegistryKeyFilterParser;
import org.inspirenxe.skills.impl.component.filter.owner.OwnerFilterParser;
import org.inspirenxe.skills.impl.component.filter.potion.PotionFilterParser;

public final class FilterModule extends AbstractModule implements FilterBinder {

  @Override
  protected void configure() {
    this.bindFilter("block").to(BlockFilterParser.class);
    this.bindFilter("cause").to(CauseFilterParser.class);
    this.bindFilter("data").to(DataFilterParser.class);
    this.bindFilter("experience").to(ExperienceFilterParser.class);
    this.bindFilter("item").to(ItemFilterParser.class);
    this.bindFilter("key").to(RegistryKeyFilterParser.class);
    this.bindFilter("level").to(LevelFilterParser.class);
    this.bindFilter("owner").to(OwnerFilterParser.class);
    this.bindFilter("namespace").to(NamespaceFilterParser.class);
    this.bindFilter("potion").to(PotionFilterParser.class);

    this.requestStaticInjection(DataFilter.class);
  }
}
