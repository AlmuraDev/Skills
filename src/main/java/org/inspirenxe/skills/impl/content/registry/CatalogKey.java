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
package org.inspirenxe.skills.impl.content.registry;

import com.almuradev.droplet.registry.RegistryKey;

public class CatalogKey implements RegistryKey {

    private final String namespace;
    private final String value;

    public CatalogKey(final String combined) {
        if (!combined.contains(":")) {
            throw new RuntimeException("Expected namespaced string");
        }

        this.namespace = combined.substring(0, combined.indexOf(':'));
        this.value = combined.substring(combined.indexOf(':') + 1);
    }

    public CatalogKey(final String namespace, final String value) {
        this.namespace = namespace;
        this.value = value;
    }

    @Override
    public String namespace() {
        return this.namespace;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.namespace + ":" + this.value;
    }
}
