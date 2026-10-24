package com.novaclient.module;

import net.minecraft.client.MinecraftClient;

/** Base type for independently switchable NOVA features. */
public abstract class FeatureModule {
    private final String id;
    private final String name;
    private final String description;
    private final String category;
    private final int defaultKeyCode;
    private boolean enabled;

    protected FeatureModule(String id, String name, String description, String category, int defaultKeyCode, boolean defaultEnabled) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.defaultKeyCode = defaultKeyCode;
        this.enabled = defaultEnabled;
    }

    public final String id() { return id; }
    public final String name() { return name; }
    public final String description() { return description; }
    public final String category() { return category; }
    public final int defaultKeyCode() { return defaultKeyCode; }
    public final boolean isEnabled() { return enabled; }

    public final void setEnabled(boolean next, MinecraftClient client) {
        if (enabled == next) return;
        enabled = next;
        if (next) onEnable(client); else onDisable(client);
    }

    protected void onEnable(MinecraftClient client) { }
    protected void onDisable(MinecraftClient client) { }
    public void tick(MinecraftClient client) { }
}
