package org.inspirenxe.skills.api;

public interface Skill {

    SkillType getSkillType();

    SkillHolder getHolder();

    double getCurrentExperience();

    Skill setExperience(double experience);

    default Skill addExperience(double experience) {
        this.setExperience(this.getCurrentExperience() + experience);
        return this;
    }

    default int getCurrentLevel() {
        return this.getSkillType().getLevelFunction().getLevelFor(this.getCurrentExperience());
    }

    /**
     * Returns if this {@link Skill} is initialized. Initialization is defined by the implementation of the skill.
     *
     * @return True if initialized, false if not
     */
    boolean isInitialized();

    /**
     * Returns if this {@link Skill} has been marked dirty and will be saved to the storage mechanism of the implementation. How this is
     * used is completely up to the implementation.
     *
     * @return True if dirty, false if not
     */
    boolean isDirtyState();

    /**
     * Sets the dirty state of the {@link Skill}. @see isDirtyState.
     *
     * @param dirtyState True if dirty, false if not
     */
    void setDirtyState(boolean dirtyState);
}
