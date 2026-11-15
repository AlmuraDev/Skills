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
import org.inspirenxe.skills.api.function.LevelFunction;

/**
 * A level function from a formula of the level {@code L}, e.g. {@code 1/8L(L-1)+75((2^((L-1)/7)-1)/(1-2^(-(1/7))))}. The experience of
 * every level up to the max level is computed once; experience past the max level stays at the max level.
 */
public final class FormulaLevelFunction implements LevelFunction {

    private final String formula;
    private final double[] xpTable;

    public FormulaLevelFunction(final String formula, final int maxLevel) {
        if (maxLevel < 1) {
            throw new IllegalArgumentException("The max level must be at least 1!");
        }
        this.formula = formula;
        final Expression expression = new ExpressionBuilder(formula).variable("L").build();
        this.xpTable = new double[maxLevel];
        for (int level = 1; level <= maxLevel; level++) {
            final double xp = expression.setVariable("L", level).evaluate();
            if (Double.isNaN(xp) || Double.isInfinite(xp)) {
                throw new IllegalArgumentException("The formula '" + formula + "' gives no number for level " + level + "!");
            }
            this.xpTable[level - 1] = xp;
        }
    }

    public String formula() {
        return this.formula;
    }

    @Override
    public double xpFor(final int level) {
        if (level < 1 || level > this.xpTable.length) {
            return LevelFunction.UNKNOWN_EXP;
        }
        return this.xpTable[level - 1];
    }

    @Override
    public int levelFor(final double xp) {
        for (int level = this.xpTable.length; level > 0; level--) {
            if (xp >= this.xpTable[level - 1]) {
                return level;
            }
        }
        return LevelFunction.UNKNOWN_LEVEL;
    }
}
