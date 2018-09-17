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
package org.inspirenxe.skills.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillHolderContainer;
import org.inspirenxe.skills.api.SkillService;
import org.inspirenxe.skills.impl.configuration.PluginConfiguration;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class SkillServiceImpl implements SkillService {

  private final PluginContainer container;
  private final Scheduler scheduler;
  private final PluginConfiguration pluginConfiguration;
  private final SkillHolderContainerImpl.Factory factory;

  private final Map<UUID, SkillHolderContainer> containers = new HashMap<>();
  private final Map<UUID, Task> tasks = new HashMap<>();
  private final Map<UUID, SaveContainerToDatabase> runnables = new HashMap<>();

  @Inject
  public SkillServiceImpl(final PluginContainer container, final Scheduler scheduler,
    final PluginConfiguration pluginConfiguration, final SkillHolderContainerImpl.Factory factory) {
    this.container = container;
    this.scheduler = scheduler;
    this.pluginConfiguration = pluginConfiguration;
    this.factory = factory;
  }

  @Override
  public DecimalFormat getXPFormat() {
    return new DecimalFormat("#,###.##");
  }

  @Override
  public Optional<SkillHolderContainer> getParentContainer(final SkillHolderContainer container) {
    checkNotNull(container);

    final String share = this.pluginConfiguration.getContainerShareConfiguration().getShare(container.getName()).orElse(null);
    if (share == null) {
      return Optional.of(container);
    }

    return this.containers.values()
      .stream()
      .filter(c -> c.getName().equals(share))
      .findFirst();
  }

  @Override
  public Map<UUID, SkillHolderContainer> getContainers() {
    return Collections.unmodifiableMap(this.containers);
  }

  @Override
  public SkillHolderContainer createContainer(final UUID containerId, final String name) {
    checkNotNull(containerId);
    checkNotNull(name);

    final SkillHolderContainerImpl container = this.factory.create(this, containerId, name);
    this.containers.put(containerId, container);

    final SaveContainerToDatabase runnable = new SaveContainerToDatabase(container);

    this.tasks.put(containerId, this.scheduler
      .createTaskBuilder()
      .async()
      .interval(this.pluginConfiguration.getSaveInterval(), TimeUnit.SECONDS)
      .execute(runnable)
      .submit(this.container)
    );

    this.runnables.put(containerId, runnable);

    return container;
  }

  @Override
  public void saveContainer(final UUID containerId) {
    checkNotNull(containerId);

    final SkillHolderContainer container = this.containers.get(containerId);
    if (container == null) {
      return;
    }

    final SaveContainerToDatabase runnable = this.runnables.get(containerId);
    if (runnable == null) {
      return;
    }

    runnable.skipProcessing(true);

    new SaveContainerToDatabase(container).run();

    runnable.skipProcessing(false);
  }

  @Override
  public Optional<SkillHolderContainer> removeContainer(final UUID containerId) {
    checkNotNull(containerId);

    final SkillHolderContainer container = this.containers.get(containerId);
    if (container == null) {
      return Optional.empty();
    }

    final SaveContainerToDatabase runnable = this.runnables.remove(containerId);

    if (runnable != null) {
      runnable.skipProcessing(true);
    }

    final Task task = this.tasks.remove(containerId);
    if (task != null) {
      task.cancel();
    }

    return Optional.of(container);
  }

  static final class SaveContainerToDatabase implements Runnable {

    private final SkillHolderContainer container;

    private boolean skipProcessing = false;

    SaveContainerToDatabase(final SkillHolderContainer container) {
      this.container = container;
    }

    @Override
    public void run() {

      if (this.skipProcessing) {
        return;
      }

      for (final SkillHolder skillHolder : this.container.getHolders().values()) {
        if (this.skipProcessing) {
          break;
        }

        skillHolder.save();
      }
    }

    public void skipProcessing(final boolean skipProcessing) {
      this.skipProcessing = skipProcessing;
    }
  }

  public interface Factory {
    SkillServiceImpl create();
  }
}
