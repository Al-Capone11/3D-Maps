package com.josem.threedmaps.platform;

import java.nio.file.Path;

/** Platform abstraction over the active mod loader (plan §2.3, §6.2). */
public interface IPlatformHelper {
    /** @return "fabric" or "neoforge" */
    String platformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    /** Root config directory (usually .minecraft/config). */
    Path getConfigDir();

    /** Game working directory (.minecraft). */
    Path getGameDir();
}
