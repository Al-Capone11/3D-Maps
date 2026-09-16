package com.josem.threedmaps.fabric;

import com.josem.threedmaps.ThreeDMaps;
import com.josem.threedmaps.platform.Services;
import net.fabricmc.api.ModInitializer;

public final class ThreeDMapsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ThreeDMaps.LOGGER.info("[{}] init on Fabric (platform={}, minecraft loaded={})",
            ThreeDMaps.MOD_NAME,
            Services.PLATFORM.platformName(),
            Services.PLATFORM.isModLoaded("minecraft"));
    }
}
