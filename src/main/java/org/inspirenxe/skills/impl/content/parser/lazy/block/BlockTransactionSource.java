package org.inspirenxe.skills.impl.content.parser.lazy.block;

import org.spongepowered.api.data.Transaction;

public enum BlockTransactionSource {

    /**
     * Looks at {@link Transaction#getOriginal()} to determine a match
     */
    ORIGINAL,
    /**
     * Looks at {@link Transaction#getOriginal()} to determine a match
     */
    FINAL,
    /**
     * Looks at both {@link Transaction#getOriginal()} and {@link Transaction#getOriginal()}
     * to determine a match
     */
    EITHER,
    /**
     * Uses the parent '<event>' tag to determine which part
     * of the transaction to look at
     */
    INHERIT;
}
