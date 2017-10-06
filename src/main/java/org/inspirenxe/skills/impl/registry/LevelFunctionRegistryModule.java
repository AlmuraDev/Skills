package org.inspirenxe.skills.impl.registry;

import static com.google.common.base.Preconditions.checkNotNull;

import org.inspirenxe.skills.api.level.LevelFunction;
import org.inspirenxe.skills.api.level.LevelFunctions;
import org.inspirenxe.skills.impl.Constants;
import org.inspirenxe.skills.impl.level.MMOStyleLevelFunction;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.RegistrationPhase;
import org.spongepowered.api.registry.util.DelayedRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class LevelFunctionRegistryModule implements AdditionalCatalogRegistryModule<LevelFunction> {

    @RegisterCatalog(LevelFunctions.class)
    private final Map<String, LevelFunction> map = new HashMap<>();

    @Override
    @DelayedRegistration(RegistrationPhase.PRE_INIT)
    public void registerDefaults() {
        this.registerAdditionalCatalog(new MMOStyleLevelFunction());
    }

    @Override
    public void registerAdditionalCatalog(LevelFunction catalogType) {
        checkNotNull(catalogType);
        this.map.put(catalogType.getId(), catalogType);
    }

    @Override
    public Optional<LevelFunction> getById(String id) {
        if (!id.contains(":")) {
            id = Constants.Plugin.ID + ":" + id;
        }
        return Optional.ofNullable(this.map.get(id));
    }

    @Override
    public Collection<LevelFunction> getAll() {
        return Collections.unmodifiableCollection(this.map.values());
    }
}
