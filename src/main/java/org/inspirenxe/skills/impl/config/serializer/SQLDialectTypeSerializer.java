/*
 * This file is part of Grand Exchange, All Rights Reserved.
 *
 * Copyright (c) AlmuraDev <http://github.com/AlmuraDev/>
 */
package org.inspirenxe.skills.impl.config.serializer;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.jooq.SQLDialect;

public class SQLDialectTypeSerializer implements TypeSerializer<SQLDialect> {

    @Override
    public SQLDialect deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        try {
            return SQLDialect.valueOf(value.getString().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ObjectMappingException("Unable to determine SQL connector to use!");
        }
    }

    @Override
    public void serialize(TypeToken<?> type, SQLDialect obj, ConfigurationNode value) throws ObjectMappingException {
        value.setValue(obj.getName().toLowerCase());
    }
}
