package com.novaclient.screen;

import com.novaclient.NovaClient;
import com.novaclient.config.ConfigManager;
import com.novaclient.config.HudElementConfig;
import com.novaclient.hud.HudRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Full-screen drag editor for configurable HUD widgets. Press H to hide/show the control panel. */
public final class HudEditorScreen extends Screen {
    private final Screen parent;
    private String selectedId;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;
    private boolean panelVisible = true;
    private String notice = "Select a widget, then drag it. Press H to hide the panel.";

    public HudEditorScreen(Screen parent) {
        super(Text.literal("NOVA HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        if (NovaClient.CONFIG == null) { super.render(context, mouseX, mouseY, delta); return; }
        if (NovaClient.CONFIG.editorGrid) drawGrid(context);
        HudRenderer.render(context, client, true);
        for (Map.Entry<String, HudElementConfig> entry : NovaClient.CONFIG.hud.entrySet()) {
            HudRenderer.HudBounds bounds = HudRenderer.bounds(client, entry.getKey());
            int border = entry.getKey().equals(selectedId) ? accent() : (entry.getValue().enabled ? 0x997D91AD : 0x665F6A7C);
            HudRenderer.drawSelectionBorder(context, bounds, border);
        }
        context.drawTextWithShadow(textRenderer, notice, 10, height - 18, 0xFFE5ECF6);
        if (panelVisible) drawPanel(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private void drawGrid(DrawContext context) {
        int color = 0x222DA0C8;
        for (int x = 0; x < width; x += 32) context.fill(x, 0, x + 1, height, color);
        for (int y = 0; y < height; y += 32) context.fill(0, y, width, y + 1, color);
    }

    private void drawPanel(DrawContext context) {
        int x = width - 238, y = 10, w = 228, h = Math.min(height - 20, 500);
        context.fill(x, y, x + w, y + h, 0xF012151D);
        context.fill(x, y, x + w, y + 3, accent());
        context.drawTextWithShadow(textRenderer, "HUD EDITOR", x + 13, y + 15, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, "H  •  Hide panel", x + 13, y + 32, 0xFF9CA8BA);
        context.fill(x + 10, y + 52, x + w - 10, y + 53, 0xFF333C4B);
        context.drawTextWithShadow(textRenderer, "Selected", x + 13, y + 64, 0xFF9CA8BA);
        context.drawTextWithShadow(textRenderer, selectedId == null ? "Click a widget" : titleCase(selectedId), x + 13, y + 80, 0xFFFFFFFF);
        HudElementConfig cfg = selectedId == null ? null : NovaClient.CONFIG.hud.get(selectedId);
        if (cfg != null) {
            context.drawTextWithShadow(textRenderer, "Status: " + (cfg.enabled ? "Enabled" : "Disabled"), x + 13, y + 103, 0xFFD5DFEC);
            context.drawTextWithShadow(textRenderer, "In menus: " + (cfg.showInMenus ? "Shown (M)" : "Hidden (M)"), x + 13, y + 116, 0xFFD5DFEC);
            context.drawTextWithShadow(textRenderer, String.format(java.util.Locale.ROOT, "Scale: %.2f", cfg.scale), x + 13, y + 132, 0xFFD5DFEC);
            context.drawTextWithShadow(textRenderer, String.format(java.util.Locale.ROOT, "Opacity: %.0f%%", cfg.opacity * 100), x + 13, y + 149, 0xFFD5DFEC);
            context.drawTextWithShadow(textRenderer, "Color: " + cfg.textColor, x + 13, y + 166, 0xFFD5DFEC);
        }
        button(context, x + 12, y + 178, 98, 28, "Enable / Disable");
        button(context, x + 118, y + 178, 98, 28, "Reset Position");
        button(context, x + 12, y + 214, 98, 28, "Scale −");
        button(context, x + 118, y + 214, 98, 28, "Scale +");
        button(context, x + 12, y + 250, 98, 28, "Opacity −");
        button(context, x + 118, y + 250, 98, 28, "Opacity +");
        button(context, x + 12, y + 286, 98, 28, "Text Color");
        button(context, x + 118, y + 286, 98, 28, "BG Color");
        button(context, x + 12, y + 322, 98, 28, "Alignment");
        button(context, x + 118, y + 322, 98, 28, "Toggle Grid");
        button(context, x + 12, y + 362, 204, 28, "Apply Balanced Preset");
        button(context, x + 12, y + 396, 204, 28, "Reset Positions & Return");
        context.drawTextWithShadow(textRenderer, "Drag widgets to any screen edge.", x + 12, y + 438, 0xFF8C9AB0);
        context.drawTextWithShadow(textRenderer, "Settings save locally as you edit.", x + 12, y + 454, 0xFF8C9AB0);
    }

    private void button(DrawContext ctx, int x, int y, int w, int h, String label) {
        ctx.fill(x, y, x + w, y + h, 0xFF293140);
        int tw = textRenderer.getWidth(label);
        ctx.drawTextWithShadow(textRenderer, label, x + (w - tw) / 2, y + (h - 8) / 2, 0xFFEAF0F8);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return super.mouseClicked(mouseX, mouseY, button);
        if (panelVisible && mouseX >= width - 238 && mouseY >= 10 && mouseY <= Math.min(height - 10, 440)) {
            int x = width - 238, y = 10;
            if (inside(mouseX, mouseY, x + 12, y + 362, 204, 28)) {
                applyBalancedPreset();
                NovaClient.saveConfig();
                return true;
            }
            if (inside(mouseX, mouseY, x + 12, y + 396, 204, 28)) {
                resetAllPositions();
                NovaClient.saveConfig();
                client.setScreen(parent);
                return true;
            }
            if (selectedId == null) { notice = "Select a HUD widget first."; return true; }
            HudElementConfig cfg = NovaClient.CONFIG.hud.get(selectedId);
            if (cfg == null) return true;
            if (inside(mouseX, mouseY, x + 12, y + 178, 98, 28)) cfg.enabled = !cfg.enabled;
            else if (inside(mouseX, mouseY, x + 118, y + 178, 98, 28)) resetSelectedPosition();
            else if (inside(mouseX, mouseY, x + 12, y + 214, 98, 28)) cfg.scale = Math.max(0.5f, cfg.scale - 0.1f);
            else if (inside(mouseX, mouseY, x + 118, y + 214, 98, 28)) cfg.scale = Math.min(2.0f, cfg.scale + 0.1f);
            else if (inside(mouseX, mouseY, x + 12, y + 250, 98, 28)) cfg.opacity = Math.max(0.15f, cfg.opacity - 0.1f);
            else if (inside(mouseX, mouseY, x + 118, y + 250, 98, 28)) cfg.opacity = Math.min(1.0f, cfg.opacity + 0.1f);
            else if (inside(mouseX, mouseY, x + 12, y + 286, 98, 28)) cycleColor(cfg);
            else if (inside(mouseX, mouseY, x + 118, y + 286, 98, 28)) cycleBackgroundColor(cfg);
            else if (inside(mouseX, mouseY, x + 12, y + 322, 98, 28)) cycleAlignment(cfg);
            else if (inside(mouseX, mouseY, x + 118, y + 322, 98, 28)) NovaClient.CONFIG.editorGrid = !NovaClient.CONFIG.editorGrid;
            else return true;
            cfg.normalize();
            NovaClient.CONFIG.activeHudPreset = "Custom";
            NovaClient.saveConfig();
            return true;
        }
        List<Map.Entry<String, HudElementConfig>> entries = new ArrayList<>(NovaClient.CONFIG.hud.entrySet());
        for (int i = entries.size() - 1; i >= 0; i--) {
            String id = entries.get(i).getKey();
            HudRenderer.HudBounds b = HudRenderer.bounds(client, id);
            if (b.contains(mouseX, mouseY)) {
                selectedId = id;
                dragging = true;
                dragOffsetX = mouseX - b.x();
                dragOffsetY = mouseY - b.y();
                notice = "Moving " + titleCase(id) + " — release to save.";
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && selectedId != null) {
            HudElementConfig cfg = NovaClient.CONFIG.hud.get(selectedId);
            if (cfg != null) {
                cfg.x = (float) Math.max(0.0, Math.min(1.0, (mouseX - dragOffsetX) / Math.max(1, width)));
                cfg.y = (float) Math.max(0.0, Math.min(1.0, (mouseY - dragOffsetY) / Math.max(1, height)));
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (dragging) {
            dragging = false;
            NovaClient.saveConfig();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_H) { panelVisible = !panelVisible; return true; }
        if (keyCode == GLFW.GLFW_KEY_M && selectedId != null) {
            HudElementConfig cfg = NovaClient.CONFIG.hud.get(selectedId);
            if (cfg != null) { cfg.showInMenus = !cfg.showInMenus; NovaClient.saveConfig(); notice = "Menu visibility: " + (cfg.showInMenus ? "shown" : "hidden"); }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { NovaClient.saveConfig(); client.setScreen(parent); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void resetSelectedPosition() {
        if (selectedId == null) return;
        HudElementConfig defaults = NovaConfigDefaults.hud(selectedId);
        if (defaults != null) { HudElementConfig cfg = NovaClient.CONFIG.hud.get(selectedId); cfg.x = defaults.x; cfg.y = defaults.y; }
    }

    private void resetAllPositions() {
        for (String id : NovaClient.CONFIG.hud.keySet()) {
            HudElementConfig defaults = NovaConfigDefaults.hud(id);
            if (defaults != null) { NovaClient.CONFIG.hud.get(id).x = defaults.x; NovaClient.CONFIG.hud.get(id).y = defaults.y; }
        }
        NovaClient.CONFIG.activeHudPreset = "Custom";
        notice = "Default widget positions restored.";
    }

    private void applyBalancedPreset() {
        // Balanced preset groups core readouts at the left and secondary readouts at the right.
        float leftY = 0.03f;
        for (String id : List.of("fps", "ping", "coordinates", "direction", "biome")) {
            HudElementConfig cfg = NovaClient.CONFIG.hud.get(id);
            if (cfg != null) { cfg.x = 0.02f; cfg.y = leftY; leftY += 0.065f; }
        }
        float rightY = 0.03f;
        for (String id : List.of("clock", "player_status", "speed", "memory")) {
            HudElementConfig cfg = NovaClient.CONFIG.hud.get(id);
            if (cfg != null) { cfg.x = 0.72f; cfg.y = rightY; rightY += 0.075f; }
        }
        HudElementConfig keystrokes = NovaClient.CONFIG.hud.get("keystrokes");
        if (keystrokes != null) { keystrokes.x = 0.02f; keystrokes.y = 0.42f; }
        HudElementConfig cps = NovaClient.CONFIG.hud.get("cps");
        if (cps != null) { cps.x = 0.02f; cps.y = 0.58f; }
        HudElementConfig armor = NovaClient.CONFIG.hud.get("armor");
        if (armor != null) { armor.x = 0.02f; armor.y = 0.65f; }
        HudElementConfig effects = NovaClient.CONFIG.hud.get("effects");
        if (effects != null) { effects.x = 0.02f; effects.y = 0.77f; }
        NovaClient.CONFIG.activeHudPreset = "Balanced";
        notice = "Balanced HUD preset applied.";
    }

    private void cycleColor(HudElementConfig cfg) {
        String[] colors = {"#FFFFFF", "#55D6FF", "#B78CFF", "#72E6A6", "#FFB86B", "#FF7893"};
        int current = 0;
        for (int i = 0; i < colors.length; i++) if (colors[i].equalsIgnoreCase(cfg.textColor)) current = i;
        cfg.textColor = colors[(current + 1) % colors.length];
    }

    private void cycleBackgroundColor(HudElementConfig cfg) {
        String[] colors = {"#151923", "#202A3B", "#102A32", "#302342", "#33261B"};
        int current = 0;
        for (int i = 0; i < colors.length; i++) if (colors[i].equalsIgnoreCase(cfg.backgroundColor)) current = i;
        cfg.backgroundColor = colors[(current + 1) % colors.length];
    }

    private void cycleAlignment(HudElementConfig cfg) {
        cfg.alignment = switch (cfg.alignment) { case "LEFT" -> "CENTER"; case "CENTER" -> "RIGHT"; default -> "LEFT"; };
    }

    private int accent() {
        try { return 0xFF000000 | Integer.parseInt(NovaClient.CONFIG.accentColor.substring(1), 16); }
        catch (Exception ignored) { return 0xFF55D6FF; }
    }

    private static String titleCase(String id) {
        StringBuilder result = new StringBuilder();
        for (String part : id.split("_")) { if (!result.isEmpty()) result.append(' '); result.append(part.substring(0, 1).toUpperCase(java.util.Locale.ROOT)).append(part.substring(1)); }
        return result.toString();
    }

    private static boolean inside(double px, double py, int x, int y, int w, int h) {
        return px >= x && py >= y && px < x + w && py < y + h;
    }

    @Override public boolean shouldPause() { return false; }

    /** Keeps default placements in one place without mutating the active configuration. */
    private static final class NovaConfigDefaults {
        static HudElementConfig hud(String id) {
            com.novaclient.config.NovaConfig defaults = com.novaclient.config.NovaConfig.defaults();
            HudElementConfig cfg = defaults.hud.get(id);
            return cfg == null ? null : cfg.copy();
        }
    }
}
