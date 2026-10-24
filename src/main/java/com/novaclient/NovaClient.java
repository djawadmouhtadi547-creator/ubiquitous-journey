package com.novaclient;

import com.novaclient.config.ConfigManager;
import com.novaclient.config.NovaConfig;
import com.novaclient.hud.HudRenderer;
import com.novaclient.keybind.KeybindManager;
import com.novaclient.module.FeatureModule;
import com.novaclient.module.ModuleManager;
import com.novaclient.screen.HudEditorScreen;
import com.novaclient.screen.NovaScreen;
import com.novaclient.util.PerformanceManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.glfw.GLFW;

public final class NovaClient implements ClientModInitializer {
    public static final String MOD_ID = "nova_client";
    public static final Logger LOGGER = LoggerFactory.getLogger("NOVA Client");
    public static NovaConfig CONFIG;
    public static ModuleManager MODULES;
    public static KeybindManager KEYBINDS;

    @Override
    public void onInitializeClient() {
        CONFIG = ConfigManager.load();
        MODULES = new ModuleManager();
        MinecraftClient client = MinecraftClient.getInstance();
        MODULES.loadFrom(CONFIG, client);
        KEYBINDS = new KeybindManager();
        KEYBINDS.applyConfig(CONFIG);
        if (CONFIG.performanceSnapshot != null && CONFIG.performanceSnapshot.valid) {
            if ("OFF".equals(CONFIG.performancePreset)) PerformanceManager.restoreOriginal(client);
            else PerformanceManager.applyPreset(client, CONFIG.performancePreset);
        }
        HudRenderer.refresh(client);

        ClientTickEvents.END_CLIENT_TICK.register(NovaClient::onClientTick);
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            HudRenderer.recordFrame();
            MinecraftClient current = MinecraftClient.getInstance();
            if (!(current.currentScreen instanceof HudEditorScreen)) HudRenderer.render(drawContext, current, false);
        });
        saveConfig();
        LOGGER.info("NOVA Client initialized for Minecraft 1.20.1.");
    }

    private static void onClientTick(MinecraftClient client) {
        if (KEYBINDS == null || MODULES == null) return;
        if (client.currentScreen == null) {
            while (KEYBINDS.openMenu().wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new NovaScreen(null));
            }
            if (client.currentScreen == null) {
                for (FeatureModule module : MODULES.all()) {
                    var key = KEYBINDS.forModule(module.id());
                    if (key != null) {
                        while (key.wasPressed()) toggleModule(module.id());
                    }
                }
            } else {
                KEYBINDS.clearPendingPresses();
            }
        } else {
            KEYBINDS.clearPendingPresses();
        }
        MODULES.tick(client);
        HudRenderer.tick(client);
    }

    public static boolean isModuleEnabled(String id) {
        return MODULES != null && MODULES.isEnabled(id);
    }

    public static void toggleModule(String id) {
        if (MODULES == null) return;
        MODULES.toggle(id, MinecraftClient.getInstance());
        saveConfig();
    }

    public static void setModuleEnabled(String id, boolean enabled) {
        if (MODULES == null) return;
        MODULES.setEnabled(id, enabled, MinecraftClient.getInstance());
        saveConfig();
    }

    public static void saveConfig() {
        if (CONFIG == null) return;
        if (MODULES != null) MODULES.writeTo(CONFIG);
        if (KEYBINDS != null) KEYBINDS.writeTo(CONFIG);
        ConfigManager.save(CONFIG);
    }

    public static void applyConfig(NovaConfig newConfig) {
        if (newConfig == null) return;
        if (CONFIG != null && CONFIG.performanceSnapshot != null && CONFIG.performanceSnapshot.valid
                && (newConfig.performanceSnapshot == null || !newConfig.performanceSnapshot.valid)) {
            PerformanceManager.restoreOriginal(MinecraftClient.getInstance());
        }
        newConfig.normalize();
        CONFIG = newConfig;
        if (MODULES != null) MODULES.loadFrom(CONFIG, MinecraftClient.getInstance());
        if (KEYBINDS != null) KEYBINDS.applyConfig(CONFIG);
        HudRenderer.refresh(MinecraftClient.getInstance());
        saveConfig();
    }

    public static void resetConfiguration() {
        applyConfig(NovaConfig.defaults());
        if (KEYBINDS != null) KEYBINDS.applyConfig(CONFIG);
        saveConfig();
    }

    public static String keyName(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN || keyCode < 0) return "Unbound";
        try {
            return net.minecraft.client.util.InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
        } catch (Exception ignored) {
            return "Key " + keyCode;
        }
    }

    public static void recordMouseClick(int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.player != null && client.currentScreen == null) HudRenderer.recordClick(button);
    }
}
