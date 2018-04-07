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

import com.almuradev.droplet.content.loader.DocumentFactory;
import com.almuradev.droplet.content.loader.finder.AbstractFoundContentEntry;
import com.almuradev.droplet.content.type.ContentBuilder;
import com.almuradev.droplet.content.type.ContentType;
import com.almuradev.droplet.registry.RegistryKey;
import com.google.common.base.Suppliers;
import net.kyori.lunar.exception.Exceptions;
import org.inspirenxe.skills.impl.registry.CatalogKey;
import org.jdom2.Element;

import java.nio.file.Path;
import java.util.function.Supplier;

public final class FoundContentEntryImpl<R extends ContentType.Root<C>, C extends ContentType.Child> extends AbstractFoundContentEntry<R, C> {

  private final String namespace;
  private final RegistryKey key;
  private final Path absolutePath;
  private final Supplier<Element> rootElement;
  private final ContentBuilder builder;

  FoundContentEntryImpl(final String namespace, final R rootType, final Path rootPath, final C childType, final Path absolutePath,
      final DocumentFactory documentFactory, final ContentBuilder builder) {
    super(rootType, childType);
    this.namespace = namespace;
    if (absolutePath.getFileName().toString().replace(".xml", "").equalsIgnoreCase(absolutePath.getParent().getFileName().toString())) {
      this.key = new CatalogKey(namespace + ':' + rootPath.relativize(absolutePath.getParent()).toString().replace(".xml", "").replace('\\', '/'));
    } else {
      this.key = new CatalogKey(namespace + ':' + rootPath.relativize(absolutePath).toString().replace(".xml", "").replace('\\', '/'));
    }
    this.absolutePath = absolutePath;
    this.rootElement = Suppliers.memoize(Exceptions.rethrowSupplier(() -> documentFactory.read(this.absolutePath).getRootElement())::get);
    this.builder = builder;
    this.builder.key(this.key);
  }

  @Override
  public String namespace() {
    return this.namespace;
  }

  @Override
  public RegistryKey key() {
    return this.key;
  }

  @Override
  public Path absolutePath() {
    return this.absolutePath;
  }

  @Override
  public Element rootElement() {
    return this.rootElement.get();
  }

  @Override
  public ContentBuilder builder() {
    return this.builder;
  }
}
