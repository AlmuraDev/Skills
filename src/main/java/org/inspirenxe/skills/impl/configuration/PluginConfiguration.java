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
package org.inspirenxe.skills.impl.configuration;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.inspirenxe.skills.impl.configuration.container.ContainerShareConfiguration;
import org.inspirenxe.skills.impl.configuration.database.DatabaseConfiguration;

public final class PluginConfiguration {

  private final DatabaseConfiguration databaseConfiguration;
  private final ContainerShareConfiguration containerShareConfiguration;
  private final int saveInterval;

  PluginConfiguration(final DatabaseConfiguration databaseConfiguration, final ContainerShareConfiguration containerShareConfiguration,
    final int saveInterval) {
    this.databaseConfiguration = checkNotNull(databaseConfiguration);
    this.containerShareConfiguration = checkNotNull(containerShareConfiguration);

    checkState(saveInterval > 0);

    this.saveInterval = saveInterval;
  }

  public DatabaseConfiguration getDatabaseConfiguration() {
    return this.databaseConfiguration;
  }

  public ContainerShareConfiguration getContainerShareConfiguration() {
    return this.containerShareConfiguration;
  }

  public int getSaveInterval() {
    return this.saveInterval;
  }
}
