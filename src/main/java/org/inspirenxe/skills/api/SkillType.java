package org.inspirenxe.skills.api;

import org.inspirenxe.skills.api.level.LevelFunction;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.util.ResettableBuilder;

public interface SkillType extends CatalogType {

    int getMinLevel();

    int getMaxLevel();

    LevelFunction getLevelFunction();

    interface Builder extends ResettableBuilder<SkillType, Builder> {

        Builder minLevel(int level);

        Builder maxLevel(int level);

        Builder levelFunction(LevelFunction function);

        SkillType build(String id, String name);
    }
}
