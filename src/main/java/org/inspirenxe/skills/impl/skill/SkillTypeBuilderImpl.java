package org.inspirenxe.skills.impl.skill;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.api.level.LevelFunction;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.level.UnknownLevelFunction;

public final class SkillTypeBuilderImpl implements SkillType.Builder {

    private int minLevel, maxLevel;
    private LevelFunction levelFunction = UnknownLevelFunction.instance;

    @Override
    public SkillType.Builder minLevel(int level) {
        this.minLevel = level;
        return this;
    }

    int minLevel() {
        return this.minLevel;
    }

    @Override
    public SkillType.Builder maxLevel(int level) {
        this.maxLevel = level;
        return this;
    }

    int maxLevel() {
        return this.maxLevel;
    }

    @Override
    public SkillType.Builder levelFunction(LevelFunction function) {
        this.levelFunction = function;
        return this;
    }

    LevelFunction levelFunction() {
        return this.levelFunction;
    }

    @Override
    public SkillType.Builder from(SkillType value) {
        this.minLevel = value.getMinLevel();
        this.maxLevel = value.getMaxLevel();
        this.levelFunction = value.getLevelFunction();
        return this;
    }

    @Override
    public SkillType.Builder reset() {
        this.minLevel = 0;
        this.maxLevel = 0;
        this.levelFunction = UnknownLevelFunction.instance;
        return this;
    }

    @Override
    public SkillType build(String id, String name) {
        checkNotNull(id);
        checkNotNull(name);
        checkNotNull(this.levelFunction);

        return new SkillTypeImpl(id, name, this);
    }
}
