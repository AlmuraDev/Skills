package org.inspirenxe.skills.api.level;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.util.annotation.CatalogedBy;

import java.util.function.Function;

/**
 * Simple {@link Function} meant to process an int-based level and return a double representing total experience at that level.
 */
@CatalogedBy(LevelFunctions.class)
public interface LevelFunction extends Function<Integer, Double>, CatalogType {

    double getXPFor(int level);

    int getLevelFor(double xp);

    default double getXPBetween(int lower, int upper) {
        return this.apply(upper) - this.apply(lower);
    }

    default Double apply(Integer value) {
        return this.getXPFor(value);
    }

    /**
     * Instructs the function to build an internal cache of the level table. It is up to the implementation to determine what
     * this method does, if anything. Even if the implementation should make use of a cache, the suggested max is not guaranteed
     * to be honored.
     *
     * @param suggestedMax A suggested maximum level to cache to
     */
    void buildLevelTable(int suggestedMax);
}
