/*
 * This file is part of raindrop, licensed under the MIT License.
 *
 * Copyright (c) 2017-2018 AlmuraDev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.inspirenxe.skills.impl.content.loader.finder;

import com.almuradev.droplet.content.loader.finder.FoundContent;
import com.almuradev.droplet.content.loader.finder.FoundContentBuilder;
import com.almuradev.droplet.content.type.ContentBuilder;
import com.almuradev.droplet.content.type.ContentType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

final class FoundContentBuilderImpl<R extends ContentType.Root<C>, C extends ContentType.Child> implements FoundContentBuilder<R, C> {
  private String namespace;
  private R root;
  private Path rootPath;
  private C child;
  private final List<FoundContent.Entry<R, C>> entries = new ArrayList<>();

  @Override
  public void namespace(final String namespace, final Path path) {
    this.namespace = namespace;
  }

  @Override
  public void root(final R root, final Path path) {
    this.root = root;
    this.rootPath = path;
  }

  @Override
  public void child(final C child, final Path path) {
    this.child = child;
  }

  @Override
  public void entry(final Path path, final Provider<ContentBuilder> builder) {
    this.entries.add(new FoundContentEntryImpl<>(
      this.namespace,
      this.root,
      this.rootPath,
      this.child,
      path.toAbsolutePath(),
      builder.get()
    ));
  }

  @Override
  public FoundContent<R, C> build() {
    return new FoundContent<>(this.entries);
  }
}
