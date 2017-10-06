package org.inspirenxe.skills.impl.level;

import org.inspirenxe.skills.impl.Constants;

public final class UnknownLevelFunction extends SkillsLevelFunction {

    public static UnknownLevelFunction instance = new UnknownLevelFunction();

    private UnknownLevelFunction() {
        super(Constants.Plugin.ID + ":unknown", "Unknown");
    }

    @Override
    public double getXPFor(int level) {
        return 0;
    }

    @Override
    public int getLevelFor(double xp) {
        return 0;
    }

    @Override
    public void buildLevelTable(int suggestedMax) {
    }
}
