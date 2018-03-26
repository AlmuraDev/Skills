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
package org.inspirenxe.skills.impl.content.loader.configuration;

import com.almuradev.droplet.content.configuration.ContentConfiguration;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

final class ContentConfigurationImpl implements ContentConfiguration {

  private final List<Path> searchPaths = new ArrayList<>();
  private final int maxDepth;

  ContentConfigurationImpl(final JsonObject config, final Path gameDir) {
    if (config.has("loader")) {
      final JsonObject loader = config.getAsJsonObject("loader");
      for (final JsonElement element : loader.getAsJsonArray("search")) {
        final String path = element.getAsString();
        if (path.charAt(0) == '/' || (path.length() > 2 && path.charAt(1) == ':')) {
          this.searchPaths.add(Paths.get(path));
        } else {
          this.searchPaths.add(gameDir.resolve(path));
        }
      }
      this.maxDepth = loader.getAsJsonPrimitive("max_depth").getAsInt();
    } else {
      this.maxDepth = 10;
    }
  }

  @Override
  public List<Path> searchPaths() {
    return this.searchPaths;
  }

  @Override
  public int maxDepth() {
    return this.maxDepth;
  }
}
