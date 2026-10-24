package com.novaclient.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HudElementConfigTest {
    @Test
    void normalizeClampsPositionScaleAndOpacity() {
        HudElementConfig config = new HudElementConfig(4.0f, -2.0f);
        config.scale = 99.0f;
        config.opacity = 0.0f;
        config.normalize();
        assertEquals(1.0f, config.x, 0.0001f);
        assertEquals(0.0f, config.y, 0.0001f);
        assertEquals(2.0f, config.scale, 0.0001f);
        assertEquals(0.15f, config.opacity, 0.0001f);
    }

    @Test
    void normalizeRepairsInvalidColors() {
        HudElementConfig config = new HudElementConfig();
        config.textColor = "not-a-color";
        config.backgroundColor = null;
        config.normalize();
        assertEquals("#FFFFFF", config.textColor);
        assertEquals("#151923", config.backgroundColor);
    }

    @Test
    void defaultsIncludeTheExpectedWidgets() {
        NovaConfig config = NovaConfig.defaults();
        config.normalize();
        assertNotNull(config.hud.get("fps"));
        assertNotNull(config.hud.get("keystrokes"));
        assertNotNull(config.hud.get("armor"));
        assertTrue(config.moduleEnabled.containsKey("zoom"));
    }
}
