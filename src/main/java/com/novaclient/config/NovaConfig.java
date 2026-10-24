package com.novaclient.config;

import java.util.LinkedHashMap;
import java.util.Map;

/** Versioned JSON settings model. Fields remain public for straightforward Gson serialization. */
public class NovaConfig {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public int schemaVersion = CURRENT_SCHEMA_VERSION;
    public String accentColor = "#55D6FF";
    public String theme = "Midnight";
    public boolean showHudInMenus = false;
    public boolean editorGrid = true;
    public double zoomFov = 28.0;
    public String activeHudPreset = "Custom";
    public String performancePreset = "OFF";
    public PerformanceSnapshot performanceSnapshot = new PerformanceSnapshot();
    public Map<String, Boolean> moduleEnabled = new LinkedHashMap<>();
    /** GLFW key codes; -1 means unbound. */
    public Map<String, Integer> keybinds = new LinkedHashMap<>();
    public Map<String, HudElementConfig> hud = new LinkedHashMap<>();

    public static NovaConfig defaults() {
        NovaConfig config = new NovaConfig();
        config.moduleEnabled.put("fullbright", false);
        config.moduleEnabled.put("zoom", false);
        config.moduleEnabled.put("toggle_sprint", false);
        config.moduleEnabled.put("dynamic_fps", false);
        config.moduleEnabled.put("low_health_warning", false);
        config.hud.put("fps", new HudElementConfig(0.02f, 0.03f));
        config.hud.put("ping", new HudElementConfig(0.02f, 0.10f));
        config.hud.put("coordinates", new HudElementConfig(0.02f, 0.17f));
        config.hud.put("direction", new HudElementConfig(0.02f, 0.24f));
        config.hud.put("biome", new HudElementConfig(0.02f, 0.31f));
        config.hud.put("keystrokes", new HudElementConfig(0.02f, 0.42f));
        config.hud.put("cps", new HudElementConfig(0.02f, 0.58f));
        config.hud.put("armor", new HudElementConfig(0.02f, 0.65f));
        config.hud.put("effects", new HudElementConfig(0.02f, 0.77f));
        config.hud.put("clock", new HudElementConfig(0.76f, 0.03f));
        config.hud.put("player_status", new HudElementConfig(0.76f, 0.10f));
        config.hud.put("speed", new HudElementConfig(0.76f, 0.17f));
        config.hud.put("memory", new HudElementConfig(0.76f, 0.24f));
        config.hud.put("frametime", new HudElementConfig(0.76f, 0.31f));
        config.hud.put("target", new HudElementConfig(0.40f, 0.78f));
        return config;
    }

    /** Repairs missing or invalid fields while preserving known user choices. */
    public void normalize() {
        schemaVersion = CURRENT_SCHEMA_VERSION;
        if (accentColor == null || !accentColor.matches("#[0-9a-fA-F]{6}")) accentColor = "#55D6FF";
        else accentColor = accentColor.toUpperCase(java.util.Locale.ROOT);
        if (theme == null || theme.isBlank()) theme = "Midnight";
        if (activeHudPreset == null || activeHudPreset.isBlank()) activeHudPreset = "Custom";
        if (!Double.isFinite(zoomFov)) zoomFov = 28.0;
        zoomFov = Math.max(15.0, Math.min(60.0, zoomFov));
        if (moduleEnabled == null) moduleEnabled = new LinkedHashMap<>();
        if (keybinds == null) keybinds = new LinkedHashMap<>();
        if (hud == null) hud = new LinkedHashMap<>();
        if (performancePreset == null || !(performancePreset.equals("OFF") || performancePreset.equals("BALANCED") || performancePreset.equals("MAX_FPS"))) performancePreset = "OFF";
        if (performanceSnapshot == null) performanceSnapshot = new PerformanceSnapshot();
        performanceSnapshot.normalize();

        NovaConfig defaults = defaults();
        defaults.hud.forEach((id, value) -> hud.putIfAbsent(id, value.copy()));
        defaults.moduleEnabled.forEach(moduleEnabled::putIfAbsent);
        for (Map.Entry<String, HudElementConfig> entry : hud.entrySet()) {
            if (entry.getValue() == null) entry.setValue(defaults.hud.getOrDefault(entry.getKey(), new HudElementConfig()).copy());
            entry.getValue().normalize();
        }
        // Avoid unreasonably large or maliciously numerous maps in imported config files.
        if (hud.size() > 64) {
            LinkedHashMap<String, HudElementConfig> trimmed = new LinkedHashMap<>();
            defaults.hud.forEach((id, value) -> trimmed.put(id, hud.getOrDefault(id, value).copy()));
            hud = trimmed;
        }
        if (moduleEnabled.size() > 128) {
            LinkedHashMap<String, Boolean> trimmed = new LinkedHashMap<>();
            defaults.moduleEnabled.forEach((id, value) -> trimmed.put(id, moduleEnabled.getOrDefault(id, value)));
            moduleEnabled = trimmed;
        }
        if (keybinds.size() > 128) keybinds = new LinkedHashMap<>();
        keybinds.replaceAll((id, key) -> key == null || key < -1 || key > 512 ? -1 : key);
    }
}
