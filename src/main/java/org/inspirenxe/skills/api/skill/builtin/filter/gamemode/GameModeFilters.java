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
package org.inspirenxe.skills.api.skill.builtin.filter.gamemode;

import net.kyori.filter.FilterQuery;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.inspirenxe.skills.api.skill.builtin.query.PlayerQuery;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;

public final class GameModeFilters {

    public static GameModeFilter adventure() {

        return new GameModeFilter() {
            @Override
            public GameMode getGameMode() {
                return GameModes.ADVENTURE;
            }

            @Override
            public boolean queryResponse(@NonNull final PlayerQuery query) {
                final boolean matched = query.getPlayer().gameMode().get() == this.getGameMode();
                if (!matched) {
                    query.denied(this);
                    return false;
                }

                return true;
            }

            @Override
            public boolean queryable(@NonNull final FilterQuery query) {
                return query instanceof PlayerQuery;
            }
        };
    }

    public static GameModeFilter creative() {

        return new GameModeFilter() {
            @Override
            public GameMode getGameMode() {
                return GameModes.CREATIVE;
            }

            @Override
            public boolean queryResponse(@NonNull final PlayerQuery query) {
                final boolean matched = query.getPlayer().gameMode().get() == this.getGameMode();
                if (!matched) {
                    query.denied(this);
                    return false;
                }

                return true;
            }

            @Override
            public boolean queryable(@NonNull final FilterQuery query) {
                return query instanceof PlayerQuery;
            }
        };
    }

    public static GameModeFilter survival() {

        return new GameModeFilter() {
            @Override
            public GameMode getGameMode() {
                return GameModes.SURVIVAL;
            }

            @Override
            public boolean queryResponse(@NonNull final PlayerQuery query) {
                final boolean matched = query.getPlayer().gameMode().get() == this.getGameMode();
                if (!matched) {
                    query.denied(this);
                    return false;
                }

                return true;
            }

            @Override
            public boolean queryable(@NonNull final FilterQuery query) {
                return query instanceof PlayerQuery;
            }
        };
    }

    private GameModeFilters() {}
}
