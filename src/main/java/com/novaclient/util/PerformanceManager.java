package com.novaclient.util;

import com.novaclient.NovaClient;
import com.novaclient.config.PerformanceSnapshot;
import com.novaclient.config.NovaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;

/** Reversible, opt-in video-option presets. These tune vanilla options; they do not add an upscaler. */
public final class PerformanceManager {
    private PerformanceManager() { }

    public static void applyPreset(MinecraftClient client, String preset) {
        if (client == null || client.options == null || NovaClient.CONFIG == null) return;
        NovaConfig config = NovaClient.CONFIG;
        if (!config.performanceSnapshot.valid) capture(client, config.performanceSnapshot);

        PerformanceSnapshot original = config.performanceSnapshot;
        if ("MAX_FPS".equals(preset)) {
            client.options.getViewDistance().setValue(Math.min(original.viewDistance, 8));
            client.options.getSimulationDistance().setValue(Math.min(original.simulationDistance, 4));
            client.options.getBiomeBlendRadius().setValue(0);
            client.options.getEntityDistanceScaling().setValue(Math.min(original.entityDistanceScaling, 0.75));
            client.options.getGraphicsMode().setValue(GraphicsMode.FAST);
            client.options.getParticles().setValue(ParticlesMode.MINIMAL);
            client.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
            client.options.getEntityShadows().setValue(false);
            client.options.getMaxFps().setValue(260);
            config.performancePreset = "MAX_FPS";
        } else if ("BALANCED".equals(preset)) {
            client.options.getViewDistance().setValue(Math.min(original.viewDistance, 12));
            client.options.getSimulationDistance().setValue(Math.min(original.simulationDistance, 8));
            client.options.getBiomeBlendRadius().setValue(Math.min(original.biomeBlendRadius, 2));
            client.options.getEntityDistanceScaling().setValue(Math.min(original.entityDistanceScaling, 1.0));
            client.options.getMaxFps().setValue(original.maxFps);
            client.options.getEntityShadows().setValue(original.entityShadows);
            try { client.options.getGraphicsMode().setValue(GraphicsMode.valueOf(original.graphicsMode)); } catch (IllegalArgumentException ignored) { }
            client.options.getParticles().setValue(ParticlesMode.DECREASED);
            client.options.getCloudRenderMode().setValue(CloudRenderMode.FAST);
            config.performancePreset = "BALANCED";
        } else {
            restoreOriginal(client);
            return;
        }
        NovaClient.saveConfig();
    }

    public static void restoreOriginal(MinecraftClient client) {
        if (client == null || client.options == null || NovaClient.CONFIG == null) return;
        NovaConfig config = NovaClient.CONFIG;
        PerformanceSnapshot snapshot = config.performanceSnapshot;
        if (snapshot != null && snapshot.valid) {
            snapshot.normalize();
            client.options.getViewDistance().setValue(snapshot.viewDistance);
            client.options.getSimulationDistance().setValue(snapshot.simulationDistance);
            client.options.getBiomeBlendRadius().setValue(snapshot.biomeBlendRadius);
            client.options.getEntityDistanceScaling().setValue(snapshot.entityDistanceScaling);
            client.options.getMaxFps().setValue(snapshot.maxFps);
            client.options.getEntityShadows().setValue(snapshot.entityShadows);
            try { client.options.getGraphicsMode().setValue(GraphicsMode.valueOf(snapshot.graphicsMode)); } catch (IllegalArgumentException ignored) { }
            try { client.options.getParticles().setValue(ParticlesMode.valueOf(snapshot.particlesMode)); } catch (IllegalArgumentException ignored) { }
            try { client.options.getCloudRenderMode().setValue(CloudRenderMode.valueOf(snapshot.cloudRenderMode)); } catch (IllegalArgumentException ignored) { }
        }
        config.performancePreset = "OFF";
        config.performanceSnapshot.valid = false;
        NovaClient.saveConfig();
    }

    public static String currentSummary(MinecraftClient client) {
        if (client == null || client.options == null) return "Waiting for game options";
        return "View " + client.options.getViewDistance().getValue()
                + " • Sim " + client.options.getSimulationDistance().getValue()
                + " • FPS cap " + (client.options.getMaxFps().getValue() >= 260 ? "Unlimited" : client.options.getMaxFps().getValue());
    }

    private static void capture(MinecraftClient client, PerformanceSnapshot snapshot) {
        snapshot.viewDistance = client.options.getViewDistance().getValue();
        snapshot.simulationDistance = client.options.getSimulationDistance().getValue();
        snapshot.biomeBlendRadius = client.options.getBiomeBlendRadius().getValue();
        snapshot.entityDistanceScaling = client.options.getEntityDistanceScaling().getValue();
        snapshot.maxFps = client.options.getMaxFps().getValue();
        snapshot.entityShadows = client.options.getEntityShadows().getValue();
        snapshot.graphicsMode = client.options.getGraphicsMode().getValue().name();
        snapshot.particlesMode = client.options.getParticles().getValue().name();
        snapshot.cloudRenderMode = client.options.getCloudRenderMode().getValue().name();
        snapshot.valid = true;
        snapshot.normalize();
    }
}
