package com.novaclient.module;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class ToggleSprintModule extends FeatureModule {
    public ToggleSprintModule() {
        super("toggle_sprint", "Toggle Sprint", "Keeps sprinting while moving forward when vanilla movement rules allow it.", "Quality of Life", GLFW.GLFW_KEY_R, false);
    }

    @Override
    public void tick(MinecraftClient client) {
        if (!isEnabled() || client.player == null || client.world == null) return;
        if (client.options.forwardKey.isPressed() && !client.options.sneakKey.isPressed()
                && !client.player.isSneaking() && !client.player.isTouchingWater()) {
            client.player.setSprinting(true);
        }
    }
}
