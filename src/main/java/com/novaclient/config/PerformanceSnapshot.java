package com.novaclient.config;

/** Original vanilla video options preserved so NOVA performance presets can be undone. */
public final class PerformanceSnapshot {
    public boolean valid = false;
    public int viewDistance = 12;
    public int simulationDistance = 8;
    public int biomeBlendRadius = 2;
    public int maxFps = 120;
    public double entityDistanceScaling = 1.0;
    public boolean entityShadows = true;
    public String graphicsMode = "FANCY";
    public String particlesMode = "ALL";
    public String cloudRenderMode = "FANCY";

    public void normalize() {
        viewDistance = clamp(viewDistance, 2, 32);
        simulationDistance = clamp(simulationDistance, 2, 32);
        biomeBlendRadius = clamp(biomeBlendRadius, 0, 7);
        maxFps = clamp(maxFps, 10, 260);
        if (!Double.isFinite(entityDistanceScaling)) entityDistanceScaling = 1.0;
        entityDistanceScaling = Math.max(0.5, Math.min(5.0, entityDistanceScaling));
        if (!oneOf(graphicsMode, "FAST", "FANCY", "FABULOUS")) graphicsMode = "FANCY";
        if (!oneOf(particlesMode, "ALL", "DECREASED", "MINIMAL")) particlesMode = "ALL";
        if (!oneOf(cloudRenderMode, "OFF", "FAST", "FANCY")) cloudRenderMode = "FANCY";
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static boolean oneOf(String value, String... allowed) {
        if (value == null) return false;
        for (String item : allowed) if (item.equals(value)) return true;
        return false;
    }
}
