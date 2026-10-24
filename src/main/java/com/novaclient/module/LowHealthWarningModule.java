package com.novaclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Shows a single action-bar warning when health falls to three hearts or less. */
public final class LowHealthWarningModule extends FeatureModule {
    private boolean warningShown;

    public LowHealthWarningModule() {
        super("low_health_warning", "Low Health Warning",
                "Shows a one-time alert at 3 hearts or less; resets after recovery.",
                "Quality of Life", GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void tick(MinecraftClient client) {
        if (!isEnabled() || client == null || client.player == null || client.world == null) {
            warningShown = false;
            return;
        }

        float health = client.player.getHealth();
        if (health <= 6.0F && !warningShown) {
            client.player.sendMessage(
                    Text.literal("§c[NOVA] Low health! Find cover or heal."), true);
            warningShown = true;
        } else if (health >= 8.0F) {
            warningShown = false;
        }
    }

    @Override
    protected void onDisable(MinecraftClient client) {
        warningShown = false;
    }
}
