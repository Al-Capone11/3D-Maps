package com.josem.threedmaps.render;

/** Decide si hace falta redibujar (§5.8.8). */
public final class MapRenderScheduler {
    private float lastCamYaw, lastCamPitch, lastCamDist, lastCX, lastCY, lastCZ;
    private long lastRenderNanos;
    private boolean anythingUploaded;

    public void markUploaded() { anythingUploaded = true; }

    public boolean needsRedraw(com.josem.threedmaps.core.camera.MapCamera cam, int refreshHz) {
        boolean moved = cam.yawDeg != lastCamYaw || cam.pitchDeg != lastCamPitch
                || cam.distance != lastCamDist
                || cam.centerX != lastCX || cam.centerY != lastCY || cam.centerZ != lastCZ;
        long minInterval = 1_000_000_000L / Math.max(1, refreshHz);
        boolean timed = (System.nanoTime() - lastRenderNanos) >= minInterval;
        boolean needed = moved || anythingUploaded || (refreshHz >= 60 /*always desatado*/);
        if (needed && (moved || anythingUploaded || timed)) {
            anythingUploaded = false;
            lastCamYaw = cam.yawDeg; lastCamPitch = cam.pitchDeg; lastCamDist = cam.distance;
            lastCX = (float)cam.centerX; lastCY = (float)cam.centerY; lastCZ = (float)cam.centerZ;
            lastRenderNanos = System.nanoTime();
            return true;
        }
        return false;
    }
}
