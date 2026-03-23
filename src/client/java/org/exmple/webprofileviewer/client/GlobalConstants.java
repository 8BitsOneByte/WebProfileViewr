package org.exmple.webprofileviewer.client;

import net.fabricmc.loader.api.FabricLoader;

public class GlobalConstants {
    public static final String NOT_FOUND = "未找到";
    public static final String NAMESPACE = "webprofileviewer";
    public static final String VERSION = resolveVersion();

    private static String resolveVersion() {
        return FabricLoader.getInstance()
            .getModContainer(NAMESPACE)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
    }
}
