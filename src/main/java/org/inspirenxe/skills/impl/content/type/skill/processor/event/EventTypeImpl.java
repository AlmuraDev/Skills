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
package org.inspirenxe.skills.impl.content.type.skill.processor.event;

import org.spongepowered.api.event.Event;

import java.util.Arrays;
import java.util.Optional;

import javax.annotation.Nullable;

public final class EventTypeImpl implements EventType {

  private final Class<? extends Event> clazz;
  private final String[] path;

  @Nullable private EventType parent;

  EventTypeImpl(final Class<? extends Event> clazz, final String path) {
    this.clazz = clazz;
    this.path = new String[1];
    Arrays.fill(this.path, path);
  }

  EventTypeImpl(final EventType parent, final Class<? extends Event> clazz, final String path) {
    this.parent = parent;
    this.clazz = clazz;
    this.path = Arrays.copyOf(parent.getPath(), parent.getPath().length + 1);
    this.path[parent.getPath().length + 1] = path;
  }

  @Override
  public Optional<EventType> getParent() {
    return Optional.ofNullable(this.parent);
  }

  @Override
  public Class<? extends Event> getEventClass() {
    return this.clazz;
  }

  @Override
  public String[] getPath() {
    return this.path;
  }

  @Override
  public boolean isExact(Class<? extends Event> clazz) {
    return this.clazz == clazz;
  }

  @Override
  public boolean isChild(Class<? extends Event> clazz) {
    // TODO
    return false;
  }
}
