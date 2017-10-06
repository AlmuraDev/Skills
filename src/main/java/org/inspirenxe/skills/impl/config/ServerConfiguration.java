package org.inspirenxe.skills.impl.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.inspirenxe.skills.impl.config.category.DatabaseCategory;

@ConfigSerializable
public final class ServerConfiguration implements Configuration {

    @Setting public final DatabaseCategory database = new DatabaseCategory();
}
