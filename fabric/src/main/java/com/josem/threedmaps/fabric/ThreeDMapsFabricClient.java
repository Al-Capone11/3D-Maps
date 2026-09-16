package com.josem.threedmaps.fabric;

import com.josem.threedmaps.ThreeDMaps;
import com.josem.threedmaps.capture.CaptureManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class ThreeDMapsFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ThreeDMaps.LOGGER.info("[{}] client init on Fabric (capturing chunks)", ThreeDMaps.MOD_NAME);
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) ->
            CaptureManager.onChunkLoad(level, chunk.getPos().x(), chunk.getPos().z()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> CaptureManager.tick(client));
    }
}
