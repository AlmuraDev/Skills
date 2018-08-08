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

import org.inspirenxe.skills.impl.content.type.skill.component.event.flatten.EventFlattener;
import org.inspirenxe.skills.impl.content.type.skill.component.event.flatten.NoOpEventFlattener;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.event.Event;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

public interface EventType<T extends Event> extends CatalogType {

    static <T extends Event> EventType<T> of(final String id, final Class<T> clazz) {
        return new EventTypeImpl<>(id, clazz, new NoOpEventFlattener<>());
    }

  static <T extends Event> EventType<T> of(final String id, final Class<T> clazz, final EventFlattener<T> flattener) {
    return new EventTypeImpl<>(id, clazz, flattener);
  }

  <S extends T> EventType<S> child(final String id, final Class<S> clazz);

    <S extends T> EventType<S> child(final String id, final Class<S> clazz, final EventFlattener<? super S> flattener);


  Optional<EventType<? super T>> getParent();

  Class<T> getEventClass();

  @Nullable
  Collection<T> flattenEvent(T event);

  List<String> getPath();

  boolean isExact(Class<? extends Event> clazz);

  boolean isDirectChild(Class<? extends Event> clazz);

  boolean matches(Class<? extends Event> clazz);

  EventFlattener<? super T> getFlattener();
}
