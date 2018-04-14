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
package org.inspirenxe.skills.impl.content.type.skill.component.event;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.spongepowered.api.event.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

public final class EventTypeImpl implements EventType {

  private final String id;
  private final Class<? extends Event> clazz;
  private final List<String> path;

  @Nullable private EventType parent;

  EventTypeImpl(final String id, final Class<? extends Event> clazz) {
    this.id = id;
    this.clazz = clazz;
    this.path = Lists.newArrayList(id);
  }

  private EventTypeImpl(final String id, final EventType parent, final Class<? extends Event> clazz) {
    this.path = new ArrayList<>(parent.getPath());
    this.path.add(id);
    this.id = String.join("/", this.path);
    this.parent = parent;
    this.clazz = clazz;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getName() {
    return this.id;
  }

  @Override
  public EventType child(final String id, final Class<? extends Event> clazz) {
    checkNotNull(id);
    checkState(this.clazz.isAssignableFrom(clazz), "Cannot create a child EventType who is not a direct child!");
    return new EventTypeImpl(id, this, clazz);
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
  public List<String> getPath() {
    return Collections.unmodifiableList(this.path);
  }

  @Override
  public boolean isExact(final Class<? extends Event> clazz) {
    return this.clazz == clazz;
  }

  @Override
  public boolean isDirectChild(final Class<? extends Event> clazz) {
    return clazz.getSuperclass() == this.clazz;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final EventTypeImpl eventType = (EventTypeImpl) o;
    return Objects.equals(id, eventType.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("id", this.id)
      .add("eventClass", this.clazz)
      .add("path", this.path)
      .add("parent", this.parent == null ? null : parent.getId())
      .toString();
  }
}
