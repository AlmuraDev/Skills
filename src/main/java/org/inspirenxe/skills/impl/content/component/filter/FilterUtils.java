package org.inspirenxe.skills.impl.content.component.filter;

import org.spongepowered.api.event.block.ChangeBlockEvent;

public class FilterUtils {

    public static void checkChangeBlockEvent(ChangeBlockEvent event) {
        if (event.getTransactions().size() != 1) {
            throw new IllegalStateException("Failed to flatten event " + event);
        }
    }

}
