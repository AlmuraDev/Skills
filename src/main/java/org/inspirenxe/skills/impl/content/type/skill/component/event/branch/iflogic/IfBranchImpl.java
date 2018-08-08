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
package org.inspirenxe.skills.impl.content.type.skill.component.event.branch.iflogic;

import net.kyori.fragment.filter.Filter;
import net.kyori.fragment.filter.FilterQuery;
import org.inspirenxe.skills.impl.content.type.skill.component.event.BranchImpl;
import org.inspirenxe.skills.impl.content.type.skill.component.event.EventData;
import org.inspirenxe.skills.impl.content.type.skill.component.event.branch.ConditionalBranch;
import org.inspirenxe.skills.impl.content.type.skill.component.event.branch.LogicBranchImpl;

import java.util.List;

final class IfBranchImpl extends LogicBranchImpl implements IfBranch {

  IfBranchImpl(Filter statement, List<ConditionalBranch> branches) {
    super(statement, branches);
  }

  @Override
  public void processInternal(EventData event) {
      boolean success = this.getStatement().allowed(event.getQuery());
      for (ConditionalBranch branch: this.getBranchesForResult(success)) {
        ((BranchImpl) branch).processInternal(event);
      }
  }
}
