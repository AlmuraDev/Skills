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
package org.inspirenxe.skills.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Driver;
import java.util.Properties;

/**
 * Loads the H2 driver bundled in the plugin jar in a class loader of its own.
 *
 * <p>SpongeAPI 21 has no SQL service, so Skills brings its own H2. Keeping it out of the plugin's class path means it can never
 * clash with an H2 another mod or plugin brings.</p>
 */
final class H2Driver {

    private static final String LIBS = "/META-INF/skills/libs/";

    private H2Driver() {
    }

    static Driver load(final Path libDir) throws Exception {
        final Properties versions = new Properties();
        try (InputStream in = H2Driver.class.getResourceAsStream("/skills-build.properties")) {
            if (in == null) {
                throw new IllegalStateException("skills-build.properties is missing from the Skills jar");
            }
            versions.load(in);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        final String name = "h2-" + versions.getProperty("h2") + ".jar";
        final Path jar = libDir.resolve(name);
        try (InputStream in = H2Driver.class.getResourceAsStream(H2Driver.LIBS + name)) {
            if (in == null) {
                throw new IllegalStateException(name + " is missing from the Skills jar");
            }
            Files.createDirectories(libDir);
            // Always refresh it, so an updated Skills jar never runs an older H2 left on disk. The file name carries the version,
            // so a jar Windows still holds open is the same H2.
            try {
                Files.copy(in, jar, StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException e) {
                if (!Files.isRegularFile(jar)) {
                    throw e;
                }
            }
        }
        // The platform class loader still sees java.sql, but nothing of Minecraft, mods or plugins
        final URLClassLoader loader = new URLClassLoader("skills-" + name, new URL[] {jar.toUri().toURL()},
                ClassLoader.getPlatformClassLoader());
        return (Driver) Class.forName("org.h2.Driver", true, loader).getDeclaredConstructor().newInstance();
    }
}
