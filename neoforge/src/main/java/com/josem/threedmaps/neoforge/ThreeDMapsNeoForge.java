package com.josem.threedmaps.neoforge;

import com.josem.threedmaps.ThreeDMaps;
import com.josem.threedmaps.platform.Services;
import net.neoforged.fml.common.Mod;

@Mod(ThreeDMaps.MOD_ID)
public final class ThreeDMapsNeoForge {
    public ThreeDMapsNeoForge() {
        ThreeDMaps.LOGGER.info("[{}] init on NeoForge (platform={}, minecraft loaded={})",
            ThreeDMaps.MOD_NAME,
            Services.PLATFORM.platformName(),
            Services.PLATFORM.isModLoaded("minecraft"));
    }
}
