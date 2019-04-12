package org.inspirenxe.skills.api.skill.builtin.util;

public class ObjectUtil {

    public static boolean dynamicEqualsCheck(final Object a, final Object b) {
        if (a instanceof String) {
            return a.toString().equalsIgnoreCase(b.toString());
        }

        if (a instanceof Enum) {
            return ((Enum) a).name().equalsIgnoreCase(b.toString());
        }

        if (a instanceof Number) {
            return a == b;
        }

        return a.equals(b);
    }

    private ObjectUtil() {}
}
