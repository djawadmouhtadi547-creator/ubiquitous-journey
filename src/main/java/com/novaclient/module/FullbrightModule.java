package com.novaclient.module;

import org.lwjgl.glfw.GLFW;

public final class FullbrightModule extends FeatureModule {
    public FullbrightModule() {
        super("fullbright", "Fullbright", "Brightens client-side lightmap rendering in dark areas.", "Visual", GLFW.GLFW_KEY_B, false);
    }
}
