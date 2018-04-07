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
package org.inspirenxe.skills.impl.component.apply.entity.player;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillManager;
import org.inspirenxe.skills.api.SkillType;
import org.spongepowered.api.entity.living.player.Player;

public final class ChangeExperiencePlayerApply implements PlayerApply {

  private final SkillManager manager;
  private final SkillType type;
  private final double experience;

  @Inject
  public ChangeExperiencePlayerApply(final SkillManager manager, @Assisted final SkillType type, @Assisted final double experience) {
    this.manager = manager;
    this.type = type;
    this.experience = experience;
  }

  @Override
  public boolean accepts(Player target) {
    return true;
  }

  @Override
  public void applyInternal(Player target) {
    // TODO World Share..
    final SkillHolder skillHolder = this.manager.getHolder(target.getWorld().getUniqueId(), target.getUniqueId()).orElse(null);
    if (skillHolder == null) {
      return;
    }
    final Skill skill = skillHolder.getSkill(this.type).orElse(null);
    if (skill == null) {
      return;
    }

    // TODO Operators
    skill.addExperience(this.experience);
  }

  interface Factory {
    ChangeExperiencePlayerApply create(final SkillType type, final double experience);
  }
}
