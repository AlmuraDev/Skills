package org.inspirenxe.skills.impl.skill.config;

public interface SkillConfig {

    String NAME = "name";

    String LEVEL = "level";

    String ACTION = "action";

    interface Level extends SkillConfig {

        String MIN = "min";

        String MAX = "max";

        String FUNCTION = "function";
    }

    interface Restrict extends SkillConfig {

        String UNTIL_LEVEL = "until_level";

        String UNTIL_EXPERIENCE = "until_experience";

        String TYPE = "type";
    }

    interface Experience extends SkillConfig {

        String AMOUNT = "amount";

        String TYPE = "type";
    }

}
