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
package org.inspirenxe.skills.function;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.inspirenxe.skills.api.function.EconomyFunction;

import java.math.BigDecimal;

/**
 * An economy function from a formula of the skill level {@code L} and the rule's money modifier {@code M}, e.g. {@code (L/100)+M}.
 */
public final class FormulaEconomyFunction implements EconomyFunction {

    private final String formula;
    // exp4j expressions keep their variables, so every thread gets its own copy
    private final ThreadLocal<Expression> expression;

    public FormulaEconomyFunction(final String formula) {
        this.formula = formula;
        // Built once up front so a broken formula fails while loading the config
        new ExpressionBuilder(formula).variables("L", "M").build();
        this.expression = ThreadLocal.withInitial(() -> new ExpressionBuilder(formula).variables("L", "M").build());
    }

    public String formula() {
        return this.formula;
    }

    @Override
    public BigDecimal moneyFor(final int level, final double modifier) {
        final double money = this.expression.get().setVariable("L", level).setVariable("M", modifier).evaluate();
        if (Double.isNaN(money) || Double.isInfinite(money)) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(money);
    }
}
