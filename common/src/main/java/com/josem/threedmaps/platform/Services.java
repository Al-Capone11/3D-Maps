package com.josem.threedmaps.platform;

import java.util.ServiceLoader;

/**
 * Loader-neutral service access point (multiloader-template pattern, plan §2.3).
 * Implementations live in each loader module and are declared via META-INF/services.
 */
public final class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    private static <T> T load(Class<T> clazz) {
        return ServiceLoader.load(clazz)
            .findFirst()
            .orElseThrow(() -> new NullPointerException("No service impl for " + clazz.getName()));
    }

    private Services() {}
}
