package com.josem.threedmaps.fabric;

import com.josem.threedmaps.ThreeDMaps;
import net.fabricmc.api.ClientModInitializer;

public final class ThreeDMapsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ThreeDMaps.LOGGER.info("[{}] client init on Fabric", ThreeDMaps.MOD_NAME);
    }
}
