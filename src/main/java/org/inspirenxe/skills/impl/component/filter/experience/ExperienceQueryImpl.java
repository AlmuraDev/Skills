package org.inspirenxe.skills.impl.component.filter.experience;

import org.inspirenxe.skills.api.Skill;

public final class ExperienceQueryImpl implements ExperienceQuery {

  private final double experience;

  public ExperienceQueryImpl(final Skill skill) {
    this.experience = skill.getCurrentExperience();
  }

  @Override
  public double experience() {
    return this.experience;
  }
}
