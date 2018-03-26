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
package org.inspirenxe.skills.impl.function.economy;

import com.almuradev.droplet.registry.RegistryKey;
import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.inspirenxe.skills.api.function.economy.EconomyFunction;
import org.inspirenxe.skills.impl.function.SkillsCatalogFunction;

import java.math.BigDecimal;
import java.util.Objects;

public final class SkillsEconomyFunction implements SkillsCatalogFunction, EconomyFunction {

  private final RegistryKey registryKey;
  private final Expression expression;

  @Inject
  public SkillsEconomyFunction(@Assisted final RegistryKey registryKey, @Assisted final String formula) {
    this.registryKey = registryKey;
    this.expression = new ExpressionBuilder(formula).variables("L", "M").build();
  }

  @Override
  public String getId() {
    return this.registryKey.toString();
  }

  @Override
  public String getName() {
    return this.registryKey.value();
  }

  @Override
  public BigDecimal getMoneyFor(int level, double modifier) {
    return BigDecimal.valueOf(this.expression.setVariable("L", level).setVariable("M", modifier).evaluate());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SkillsEconomyFunction that = (SkillsEconomyFunction) o;
    return Objects.equals(this.registryKey, that.registryKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.registryKey);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", this.registryKey)
        .add("formula", this.expression)
        .toString();
  }


  public interface Factory {

    SkillsEconomyFunction create(final RegistryKey registryKey, final String formula);
  }
}
