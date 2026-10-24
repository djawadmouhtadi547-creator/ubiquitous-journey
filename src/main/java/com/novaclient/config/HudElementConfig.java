package com.novaclient.config;

/** Persistent, user-editable settings for a single NOVA HUD widget. */
public class HudElementConfig {
    public boolean enabled = true;
    /** Normalized position in the range 0..1. */
    public float x = 0.02f;
    public float y = 0.03f;
    public float scale = 1.0f;
    public float opacity = 0.85f;
    public String textColor = "#FFFFFF";
    public String backgroundColor = "#151923";
    public String alignment = "LEFT";
    public boolean showInMenus = false;

    public HudElementConfig() { }

    public HudElementConfig(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public HudElementConfig copy() {
        HudElementConfig copy = new HudElementConfig(x, y);
        copy.enabled = enabled;
        copy.scale = scale;
        copy.opacity = opacity;
        copy.textColor = textColor;
        copy.backgroundColor = backgroundColor;
        copy.alignment = alignment;
        copy.showInMenus = showInMenus;
        return copy;
    }

    public void normalize() {
        x = clamp(x, 0.0f, 1.0f);
        y = clamp(y, 0.0f, 1.0f);
        scale = clamp(scale, 0.5f, 2.0f);
        opacity = clamp(opacity, 0.15f, 1.0f);
        textColor = validColor(textColor, "#FFFFFF");
        backgroundColor = validColor(backgroundColor, "#151923");
        if (alignment == null || !(alignment.equalsIgnoreCase("LEFT") || alignment.equalsIgnoreCase("CENTER") || alignment.equalsIgnoreCase("RIGHT"))) alignment = "LEFT";
        else alignment = alignment.toUpperCase(java.util.Locale.ROOT);
    }

    private static float clamp(float value, float min, float max) {
        if (!Float.isFinite(value)) return min;
        return Math.max(min, Math.min(max, value));
    }

    private static String validColor(String value, String fallback) {
        if (value == null || !value.matches("#[0-9a-fA-F]{6}")) return fallback;
        return value.toUpperCase(java.util.Locale.ROOT);
    }
}
