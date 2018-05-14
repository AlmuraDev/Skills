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

import org.inspirenxe.skills.impl.database.DatabaseUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public final class DatabaseUtilsTest {
  @Test
  public void verifyUUIDBytesCanBeReconstructedBackToOriginalUUID() {
    final UUID u = UUID.randomUUID();
    final byte[] uBytes = DatabaseUtils.toBytes(u);
    final UUID u2 = DatabaseUtils.fromBytes(uBytes);
    Assert.assertEquals(u, u2);
  }

  @Test
  public void verifyNameUUIDFromBytesMethodDoesNotRecreateOriginalUUID() {
    final UUID u = UUID.randomUUID();
    final byte[] uBytes = DatabaseUtils.toBytes(u);
    final UUID u2 = UUID.nameUUIDFromBytes(uBytes);
    Assert.assertNotEquals(u, u2);
  }
}
