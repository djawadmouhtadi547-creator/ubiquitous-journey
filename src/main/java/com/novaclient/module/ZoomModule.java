package com.novaclient.module;

import com.novaclient.NovaClient;
import org.lwjgl.glfw.GLFW;

public final class ZoomModule extends FeatureModule {
    public ZoomModule() {
        super("zoom", "Zoom", "Reduces camera field of view while enabled.", "Visual", GLFW.GLFW_KEY_C, false);
    }

    public static boolean isZoomActive() {
        return NovaClient.isModuleEnabled("zoom");
    }
}
