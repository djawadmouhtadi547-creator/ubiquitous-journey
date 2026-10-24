package com.novaclient.module;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/** Reduces the frame cap while the game window is unfocused; restores it on focus. */
public final class DynamicFpsModule extends FeatureModule {
    private int previousMaxFps = 120;
    private boolean capped;

    public DynamicFpsModule() {
        super("dynamic_fps", "Dynamic FPS", "Caps FPS to 30 while the game window is unfocused, then restores your cap.", "Performance", GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void tick(MinecraftClient client) {
        if (!isEnabled() || client == null || client.options == null || client.getWindow() == null) return;
        long handle = client.getWindow().getHandle();
        if (handle == 0L) return;
        boolean focused = GLFW.glfwGetWindowAttrib(handle, GLFW.GLFW_FOCUSED) == GLFW.GLFW_TRUE;
        if (!focused && !capped) {
            previousMaxFps = client.options.getMaxFps().getValue();
            if (previousMaxFps != 30) client.options.getMaxFps().setValue(30);
            capped = true;
        } else if (focused && capped) {
            client.options.getMaxFps().setValue(previousMaxFps);
            capped = false;
        }
    }

    @Override
    protected void onDisable(MinecraftClient client) {
        if (capped && client != null && client.options != null) {
            client.options.getMaxFps().setValue(previousMaxFps);
        }
        capped = false;
    }
}
