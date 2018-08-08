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
import org.inspirenxe.skills.impl.content.type.skill.component.event.flatten.EventFlattener;
import org.spongepowered.api.event.Event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

public final class EventTypeImpl<T extends Event> implements EventType<T> {

  private final String id;
  private final Class<T> clazz;
  private final List<String> path;
  private final EventFlattener<? super T> flattener;

  @Nullable private EventType<? super T> parent;

  EventTypeImpl(final String id, final Class<T> clazz, EventFlattener<? super T> flattener) {
    this.id = id;
    this.clazz = clazz;
    this.path = Lists.newArrayList(id);
    this.flattener = flattener;
  }

  private EventTypeImpl(final String id, final EventType<? super T> parent, final Class<T> clazz, EventFlattener<? super T> flattener) {
    this.path = new ArrayList<>(parent.getPath());
    this.path.add(id);
    this.id = String.join("/", this.path);
    this.parent = parent;
    this.clazz = clazz;
    this.flattener = flattener;
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
  public <S extends T> EventType<S> child(final String id, final Class<S> clazz) {
    return this.child(id, clazz, this.flattener);
  }

  @Override
  public <S extends T> EventType<S> child(final String id, final Class<S> clazz, final EventFlattener<? super S> flattener) {
    checkNotNull(id);
    checkState(this.clazz.isAssignableFrom(clazz), "Cannot create a child EventType who is not a direct child!");
    return new EventTypeImpl<S>(id, this, clazz, flattener);
  }
  @Override
  public Optional<EventType<? super T>> getParent() {
    return Optional.ofNullable(this.parent);
  }

  @Override
  public Class<T> getEventClass() {
    return this.clazz;
  }

    @Override
    public Collection<T> flattenEvent(Event event) {
        return null;
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
  public boolean matches(Class<? extends Event> eventClass) {
    return this.clazz.isAssignableFrom(eventClass);
  }

    @Override
    public EventFlattener<? super T> getFlattener() {
        return this.flattener;
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
