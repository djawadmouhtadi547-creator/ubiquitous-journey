package com.novaclient.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PerformanceSnapshotTest {
    @Test
    void normalizeClampsSavedVanillaOptions() {
        PerformanceSnapshot snapshot = new PerformanceSnapshot();
        snapshot.viewDistance = 999;
        snapshot.simulationDistance = -10;
        snapshot.biomeBlendRadius = 50;
        snapshot.maxFps = 999;
        snapshot.entityDistanceScaling = Double.NaN;
        snapshot.normalize();
        assertEquals(32, snapshot.viewDistance);
        assertEquals(2, snapshot.simulationDistance);
        assertEquals(7, snapshot.biomeBlendRadius);
        assertEquals(260, snapshot.maxFps);
        assertEquals(1.0, snapshot.entityDistanceScaling, 0.0001);
    }

    @Test
    void normalizeRepairsUnknownEnumNames() {
        PerformanceSnapshot snapshot = new PerformanceSnapshot();
        snapshot.graphicsMode = "ULTRA_MAGIC";
        snapshot.particlesMode = null;
        snapshot.cloudRenderMode = "INFINITE";
        snapshot.normalize();
        assertEquals("FANCY", snapshot.graphicsMode);
        assertEquals("ALL", snapshot.particlesMode);
        assertEquals("FANCY", snapshot.cloudRenderMode);
    }

    @Test
    void defaultsCreatePerformanceSnapshotAndFrameTimeWidget() {
        NovaConfig config = NovaConfig.defaults();
        config.normalize();
        assertNotNull(config.performanceSnapshot);
        assertNotNull(config.hud.get("frametime"));
    }
}
