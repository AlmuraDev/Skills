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
package org.inspirenxe.skills.impl.content.loader.finder;

import com.almuradev.droplet.content.loader.ChildContentLoader;
import com.almuradev.droplet.content.loader.DocumentFactory;
import com.almuradev.droplet.content.loader.finder.FoundContent;
import com.almuradev.droplet.content.loader.finder.FoundContentBuilder;
import com.almuradev.droplet.content.type.ContentBuilder;
import com.almuradev.droplet.content.type.ContentType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

final class FoundContentBuilderImpl<R extends ContentType.Root<C>, C extends ContentType.Child> implements FoundContentBuilder<R, C> {

  private final List<FoundContent.Entry<R, C>> entries = new ArrayList<>();
  private String namespace;
  private Path namespacePath;
  private R root;
  private Path rootPath;
  private ChildContentLoader<C> childLoader;
  private C child;

  @Override
  public void namespace(final String namespace, final Path namespacePath) {
    this.namespace = namespace;
    this.namespacePath = namespacePath;
  }

  @Override
  public void root(final R root, final Path path) {
    this.root = root;
    this.rootPath = path;
  }

  @Override
  public void child(final ChildContentLoader<C> loader, final C child, final Path path) {
    this.childLoader = loader;
    this.child = child;
  }

  @Override
  public void entry(final Path path, final Provider<ContentBuilder> builder) {
    final FoundContent.Entry<R, C> entry = new FoundContentEntryImpl<>(
        this.namespace,
        this.root,
        this.rootPath,
        this.child,
        path.toAbsolutePath(),
        new DocumentFactory(Collections.singletonList(this.rootPath)),
        builder.get()
    );
    this.childLoader.foundContent().offer(entry);
    this.entries.add(entry);
  }

  @Override
  public FoundContent<R, C> build() {
    return new FoundContent<>(this.entries);
  }
}