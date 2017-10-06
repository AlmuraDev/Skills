package org.inspirenxe.skills.impl.skill;

import com.google.common.base.MoreObjects;
import org.inspirenxe.skills.api.level.LevelFunction;
import org.inspirenxe.skills.api.SkillType;

import java.util.Objects;

public class SkillTypeImpl implements SkillType {

    private final String id, name;
    private final int minlevel, maxLevel;
    private final LevelFunction levelFunction;

    protected SkillTypeImpl(String id, String name, SkillTypeBuilderImpl builder) {
        this.id = id;
        this.name = name;
        this.minlevel = builder.minLevel();
        this.maxLevel = builder.maxLevel();
        this.levelFunction = builder.levelFunction();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getMinLevel() {
        return this.minlevel;
    }

    @Override
    public int getMaxLevel() {
        return this.maxLevel;
    }

    @Override
    public LevelFunction getLevelFunction() {
        return this.levelFunction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SkillTypeImpl)) {
            return false;
        }
        final SkillTypeImpl skillType = (SkillTypeImpl) o;
        return Objects.equals(id, skillType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .add("name", this.name)
                .add("minLevel", this.minlevel)
                .add("maxLevel", this.maxLevel)
                .add("levelFunction", this.levelFunction)
                .toString();
    }
}
