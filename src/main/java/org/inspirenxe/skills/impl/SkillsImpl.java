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

import com.google.inject.Inject;
import com.google.inject.Injector;
import net.kyori.membrane.facet.internal.Facets;
import org.inspirenxe.skills.impl.content.ContentModule;
import org.slf4j.Logger;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import javax.annotation.Nullable;

@Plugin(id = SkillsImpl.ID)
public final class SkillsImpl {

  public static final String ID = "skills";

  private final Logger logger;
  private final Path configDir;
  @Nullable private Facets facets;

  @Inject
  public SkillsImpl(final Injector baseInjector, final Logger logger, final @ConfigDir(sharedRoot = false) Path configDir)
    throws IOException, URISyntaxException {
    this.logger = logger;
    this.configDir = configDir;

    this.writeDefaultAssets();

    this.facets = baseInjector.createChildInjector(new ContentModule(), new ToolboxModule(), new SkillsModule()).getInstance(Facets.class);
  }

  @Listener(order = Order.PRE)
  public void onGameConstruction(final GameConstructionEvent event) {
    if (this.facets != null) {
      this.facets.enable();
    }
  }

  @Listener
  public void onGameStopping(final GameStoppingEvent event) {
    if (this.facets != null) {
      this.facets.disable();
    }
  }

  private void writeDefaultAssets() throws IOException, URISyntaxException {
    this.logger.info("Writing missing assets to '" + this.configDir + "'");
    final URI uri = SkillsImpl.class.getResource("/assets/" + SkillsImpl.ID).toURI();

    final Path path;

    if (uri.getScheme().equals("jar")) {
      final FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
      path = fileSystem.getPath("/assets/" + SkillsImpl.ID);
    } else {
      path = Paths.get(uri);
    }

    Files.walkFileTree(path, new DefaultFileVisitor());
  }

  private final class DefaultFileVisitor extends SimpleFileVisitor<Path> {

    @Override
    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
      final int index = this.startIndex(dir);
      final Path actual;

      if (index == dir.getNameCount()) {
        actual = SkillsImpl.this.configDir;
      } else {
        actual = SkillsImpl.this.configDir.resolve(dir.subpath(index, dir.getNameCount()));
      }

      if (Files.notExists(actual)) {
        return FileVisitResult.CONTINUE;
      }

      return FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
      final int index = this.startIndex(file);
      final Path actual;

      if (index == file.getNameCount()) {
        actual = SkillsImpl.this.configDir;
      } else {
        actual = SkillsImpl.this.configDir.resolve(file.subpath(index, file.getNameCount()));
      }

      if (Files.notExists(actual)) {
        Files.createDirectories(actual.getParent());
        Files.copy(file, actual);
      }

      return super.visitFile(file, attrs);
    }

    private int startIndex(Path path) {
      int nameCount = path.getNameCount();

      while (nameCount > 0) {
        if (path.getFileName().toString().equalsIgnoreCase(SkillsImpl.ID)) {
          return nameCount;
        }

        nameCount--;
        path = path.subpath(0, nameCount);
      }

      return -1;
    }
  }
}
