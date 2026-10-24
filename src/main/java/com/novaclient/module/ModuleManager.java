package com.novaclient.module;

import com.novaclient.config.NovaConfig;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ModuleManager {
    private final Map<String, FeatureModule> modules = new LinkedHashMap<>();

    public ModuleManager() {
        register(new FullbrightModule());
        register(new ZoomModule());
        register(new ToggleSprintModule());
        register(new DynamicFpsModule());
        register(new LowHealthWarningModule());
    }

    private void register(FeatureModule module) {
        modules.put(module.id(), module);
    }

    public List<FeatureModule> all() {
        return Collections.unmodifiableList(new ArrayList<>(modules.values()));
    }

    public FeatureModule get(String id) { return modules.get(id); }

    public boolean isEnabled(String id) {
        FeatureModule module = modules.get(id);
        return module != null && module.isEnabled();
    }

    public void setEnabled(String id, boolean enabled, MinecraftClient client) {
        FeatureModule module = modules.get(id);
        if (module != null) module.setEnabled(enabled, client);
    }

    public void toggle(String id, MinecraftClient client) {
        FeatureModule module = modules.get(id);
        if (module != null) module.setEnabled(!module.isEnabled(), client);
    }

    public void loadFrom(NovaConfig config, MinecraftClient client) {
        for (FeatureModule module : modules.values()) {
            boolean enabled = config.moduleEnabled.getOrDefault(module.id(), false);
            module.setEnabled(enabled, client);
            config.moduleEnabled.put(module.id(), enabled);
        }
    }

    public void writeTo(NovaConfig config) {
        for (FeatureModule module : modules.values()) config.moduleEnabled.put(module.id(), module.isEnabled());
    }

    public void tick(MinecraftClient client) {
        for (FeatureModule module : modules.values()) module.tick(client);
    }
}
