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
package org.inspirenxe.skills.api.skill.builtin.applicator;

import com.almuradev.toolbox.util.math.DoubleRange;
import org.inspirenxe.skills.api.function.economy.EconomyFunctionType;
import org.inspirenxe.skills.api.skill.builtin.query.AbstractEventQuery;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;

import java.math.BigDecimal;
import java.util.Random;

public final class EconomyApplicators {

    private EconomyApplicators() {
    }

    public static Applicator money(final EconomyService service, final Currency currency, final double amount) {
        return query -> {
            if (!(query instanceof AbstractEventQuery)) {
                return;
            }

            final AbstractEventQuery eventQuery = (AbstractEventQuery) query;
            eventQuery.getCause().first(User.class).ifPresent(user -> {
                if (service.hasAccount(user.getUniqueId())) {
                    service.getOrCreateAccount(user.getUniqueId()).get().deposit(currency, BigDecimal.valueOf(amount), eventQuery.getCause());
                }
            });
        };
    }

    public static Applicator scaledMoney(final EconomyService service, final EconomyFunctionType function, final Currency currency,
        final double base) {
        return query -> {
            if (!(query instanceof AbstractEventQuery)) {
                return;
            }

            final AbstractEventQuery eventQuery = (AbstractEventQuery) query;
            eventQuery.getCause().first(User.class).ifPresent(user -> {
                if (service.hasAccount(user.getUniqueId())) {
                    service.getOrCreateAccount(user.getUniqueId()).get()
                        .deposit(currency, function.getMoneyFor(eventQuery.getSkill().getCurrentLevel(), base), eventQuery.getCause());
                }
            });
        };
    }

    public static Applicator variableScaledMoney(final EconomyService service, final EconomyFunctionType function, final Currency currency,
        final DoubleRange range, final Random random) {
        return query -> {
            if (!(query instanceof AbstractEventQuery)) {
                return;
            }

            final AbstractEventQuery eventQuery = (AbstractEventQuery) query;
            eventQuery.getCause().first(User.class).ifPresent(user -> {
                if (service.hasAccount(user.getUniqueId())) {
                    service.getOrCreateAccount(user.getUniqueId()).get()
                        .deposit(currency, function.getMoneyFor(eventQuery.getSkill().getCurrentLevel(), range.random(random)),
                            eventQuery.getCause());
                }
            });
        };
    }
}