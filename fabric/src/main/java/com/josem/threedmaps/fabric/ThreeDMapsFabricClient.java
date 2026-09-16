package com.josem.threedmaps.fabric;

import com.josem.threedmaps.ThreeDMaps;
import com.josem.threedmaps.capture.CaptureManager;
import com.josem.threedmaps.core.render.IMapRenderBackend;
import com.josem.threedmaps.render.GpuUploadQueue;
import com.josem.threedmaps.render.backend.Blaze3dBackend262;
import com.josem.threedmaps.ui.FullMapScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class ThreeDMapsFabricClient implements ClientModInitializer {
    private static final GpuUploadQueue.Registry REGISTRY = new GpuUploadQueue.Registry();
    private static IMapRenderBackend backend;
    private static KeyMapping keyOpenMap;

    @Override
    public void onInitializeClient() {
        ThreeDMaps.LOGGER.info("[{}] client init on Fabric", ThreeDMaps.MOD_NAME);
        backend = new Blaze3dBackend262();

        keyOpenMap = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.threedmaps.open_map",
            com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            KeyMapping.Category.MISC
        ));

        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) ->
            CaptureManager.onChunkLoad(level, chunk.getPos().x(), chunk.getPos().z()));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CaptureManager.tick(client);
            if (keyOpenMap != null && keyOpenMap.consumeClick() && client.level != null) {
                client.gui.setScreen(new FullMapScreen(backend, REGISTRY));
            }
        });
    }
}
