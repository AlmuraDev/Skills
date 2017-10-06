package org.inspirenxe.skills.impl.database;

import org.jooq.DSLContext;
import org.jooq.Query;

@FunctionalInterface
public interface DatabaseQuery<Q extends Query> {

    Q build(DSLContext context);
}
