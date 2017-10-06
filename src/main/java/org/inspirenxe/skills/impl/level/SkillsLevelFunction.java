package org.inspirenxe.skills.impl.level;

import com.google.common.base.MoreObjects;
import org.inspirenxe.skills.api.level.LevelFunction;

import java.util.Objects;

public abstract class SkillsLevelFunction implements LevelFunction {

    private final String id, name;

    SkillsLevelFunction(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public final String getId() {
        return this.id;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SkillsLevelFunction that = (SkillsLevelFunction) o;
        return Objects.equals(id, that.id);
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
                .toString();
    }
}
