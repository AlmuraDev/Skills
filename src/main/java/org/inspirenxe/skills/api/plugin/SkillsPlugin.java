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
package org.inspirenxe.skills.api.plugin;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;

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

public abstract class SkillsPlugin {

    private final String id;
    private final Logger logger;
    private final Path configDir;

    public SkillsPlugin(final String id, final Logger logger, final Path configDir) {
        this.id = checkNotNull(id);
        this.logger = checkNotNull(logger);
        this.configDir = checkNotNull(configDir);
    }

    protected void writeDefaultAssets() throws IOException, URISyntaxException {
        this.logger.info("Writing missing assets to '" + this.configDir + "'");
        final URI uri = SkillsPlugin.this.getClass().getResource("/assets/" + this.id).toURI();

        FileSystem fileSystem = null;
        final Path path;

        try {
            if (uri.getScheme().equals("jar")) {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                path = fileSystem.getPath("/assets/" + this.id);
            } else {
                path = Paths.get(uri);
            }

            Files.walkFileTree(path.normalize(), new DefaultFileVisitor());

        } finally {
            if (fileSystem != null) {
                fileSystem.close();
            }
        }
    }

    private final class DefaultFileVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
            final int index = this.startIndex(dir);
            final Path actual;

            if (index == dir.getNameCount()) {
                return FileVisitResult.CONTINUE;
            } else {
                actual = SkillsPlugin.this.configDir.resolve(dir.subpath(index, dir.getNameCount()).toString());

                if (Files.exists(actual)) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            final int index = this.startIndex(file);
            final Path actual;

            if (index == file.getNameCount()) {
                actual = SkillsPlugin.this.configDir;
            } else {
                actual = SkillsPlugin.this.configDir.resolve(file.subpath(index, file.getNameCount()).toString());
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
                if (path.getFileName().toString().equalsIgnoreCase(SkillsPlugin.this.id)) {
                    return nameCount;
                }

                nameCount--;
                path = path.subpath(0, nameCount);
            }

            return -1;
        }
    }
}
