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
package org.inspirenxe.skills.impl.content.type.skill.component.event.branch;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.inspirenxe.skills.impl.content.component.apply.EventApplicator;
import org.inspirenxe.skills.impl.content.type.skill.component.event.BranchBuilder;

import java.util.ArrayList;
import java.util.List;

public final class ResultBranchBuilder extends BranchBuilder<ResultBranch> implements ResultBranch.Builder {

  private final List<EventApplicator> applicators = new ArrayList<>();

  @Override
  public ResultBranch.Builder apply(final EventApplicator eventApplicator) {
    checkNotNull(eventApplicator);

    this.applicators.add(eventApplicator);
    return this;
  }

  @Override
  public ResultBranch build() {
    //checkState(!this.applicators.isEmpty(), "A result branch that does nothing makes no sense!");
    return new ResultBranchImpl(this.applicators);
  }
}
