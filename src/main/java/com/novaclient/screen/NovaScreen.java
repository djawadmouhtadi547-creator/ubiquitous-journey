package com.novaclient.screen;

import com.novaclient.NovaClient;
import com.novaclient.config.ConfigManager;
import com.novaclient.config.HudElementConfig;
import com.novaclient.config.NovaConfig;
import com.novaclient.hud.HudRenderer;
import com.novaclient.module.FeatureModule;
import com.novaclient.util.PerformanceManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Main NOVA settings screen. All controls shown here perform a real local action. */
public final class NovaScreen extends Screen {
    private final Screen parent;
    private String page = "Dashboard";
    private String category = "All";
    private String search = "";
    private TextFieldWidget searchField;
    private String notice = "";
    private String rebindingId;

    public NovaScreen(Screen parent) {
        super(Text.literal("NOVA Client"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        searchField = null;
        if (page.equals("Modules")) {
            int x = panelX() + 170;
            int w = panelWidth() - 194;
            searchField = new TextFieldWidget(textRenderer, x + 18, panelY() + 115, Math.max(100, w - 36), 20, Text.literal("Search modules"));
            searchField.setMaxLength(64);
            searchField.setPlaceholder(Text.literal("Search modules…"));
            searchField.setText(search);
            searchField.setChangedListener(value -> search = value);
            addDrawableChild(searchField);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        int x = panelX(), y = panelY(), w = panelWidth(), h = panelHeight();
        int accent = accentColor();
        context.fill(x, y, x + w, y + h, 0xF012151D);
        context.fill(x, y, x + w, y + 3, accent);
        context.fill(x, y, x + 1, y + h, 0xFF2A3040);
        context.fill(x + w - 1, y, x + w, y + h, 0xFF2A3040);
        context.fill(x, y + h - 1, x + w, y + h, 0xFF2A3040);
        context.fill(x, y + 3, x + w, y + 54, 0xFF191E29);
        context.drawTextWithShadow(textRenderer, "NOVA", x + 20, y + 17, accent);
        context.drawTextWithShadow(textRenderer, "CLIENT  /  1.1.0", x + 75, y + 17, 0xFFBFC9D8);
        context.drawTextWithShadow(textRenderer, "MINECRAFT 1.20.1", x + w - 164, y + 17, 0xFF8995A8);
        drawButton(context, x + w - 34, y + 12, 22, 22, "×", 0xFF293140, 0xFFFFFFFF);

        String[] nav = {"Dashboard", "Modules", "HUD Editor", "Performance", "Settings", "About"};
        for (int i = 0; i < nav.length; i++) {
            int ny = y + 76 + i * 42;
            boolean selected = page.equals(nav[i]);
            if (selected) context.fill(x + 10, ny - 5, x + 154, ny + 27, 0xFF252C3A);
            if (selected) context.fill(x + 10, ny - 5, x + 13, ny + 27, accent);
            context.drawTextWithShadow(textRenderer, nav[i], x + 24, ny + 5, selected ? 0xFFFFFFFF : 0xFF9CA8BA);
        }
        context.fill(x + 163, y + 65, x + 164, y + h - 35, 0xFF2A3040);
        context.drawTextWithShadow(textRenderer, "FREE  •  OPEN SOURCE", x + 18, y + h - 26, 0xFF738096);
        int contentX = x + 184;
        int contentY = y + 75;
        int contentW = w - 204;
        int contentH = h - 100;

        switch (page) {
            case "Dashboard" -> renderDashboard(context, contentX, contentY, contentW, contentH);
            case "Modules" -> renderModules(context, contentX, contentY, contentW, contentH);
            case "HUD Editor" -> renderHudPage(context, contentX, contentY, contentW, contentH);
            case "Performance" -> renderPerformance(context, contentX, contentY, contentW, contentH);
            case "Settings" -> renderSettings(context, contentX, contentY, contentW, contentH);
            case "About" -> renderAbout(context, contentX, contentY, contentW, contentH);
            default -> { }
        }
        if (!notice.isBlank()) context.drawTextWithShadow(textRenderer, notice, contentX, y + h - 23, 0xFF8FE7C2);
        if (rebindingId != null) {
            context.fill(x + 170, y + h - 54, x + w - 16, y + h - 29, 0xFF2D3A51);
            context.drawTextWithShadow(textRenderer, "Press a key  •  ESC cancels  •  DELETE unbinds", x + 182, y + h - 46, 0xFFFFFFFF);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderDashboard(DrawContext ctx, int x, int y, int w, int h) {
        ctx.drawTextWithShadow(textRenderer, "Welcome to NOVA", x, y, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "A modular, client-side companion for Minecraft 1.20.1.", x, y + 17, 0xFF98A5B8);
        card(ctx, x, y + 48, w, 86, "YOUR CLIENT", "Fabric  •  Java 17  •  Local settings", "No account, paid service or telemetry", accentColor());
        int enabled = 0;
        if (NovaClient.MODULES != null) for (FeatureModule module : NovaClient.MODULES.all()) if (module.isEnabled()) enabled++;
        int half = (w - 12) / 2;
        card(ctx, x, y + 146, half, 78, "MODULES ACTIVE", enabled + " / " + (NovaClient.MODULES == null ? 0 : NovaClient.MODULES.all().size()) + " enabled", "Toggle features individually", 0xFF7D9BFF);
        card(ctx, x + half + 12, y + 146, half, 78, "HUD WIDGETS", NovaClient.CONFIG.hud.size() + " configurable widgets", "Move and style them in the editor", 0xFFBD8CFF);
        drawButton(ctx, x, y + 241, 132, 30, "Browse Modules", 0xFF252D3B, 0xFFFFFFFF);
        drawButton(ctx, x + 142, y + 241, 132, 30, "Edit HUD", 0xFF252D3B, 0xFFFFFFFF);
        drawButton(ctx, x + 284, y + 241, 132, 30, "Client Settings", 0xFF252D3B, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Tip: press Right Shift to open NOVA. See Performance for reversible FPS presets.", x, y + 292, 0xFF8491A4);
    }

    private void renderModules(DrawContext ctx, int x, int y, int w, int h) {
        ctx.drawTextWithShadow(textRenderer, "Modules", x, y, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Search, toggle, and hotkey the included client-side features.", x, y + 17, 0xFF98A5B8);
        String[] categories = {"All", "Visual", "Quality", "Perf"};
        int categoryWidth = Math.min(74, Math.max(42, (w - 12) / categories.length));
        for (int i = 0; i < categories.length; i++) {
            int bx = x + i * (categoryWidth + 4);
            String categoryId = categories[i].equals("Perf") ? "Performance" : categories[i];
            drawButton(ctx, bx, y + 42, categoryWidth, 24, categories[i], category.equals(categoryId) ? accentColor() : 0xFF252D3B, category.equals(categoryId) ? 0xFF101620 : 0xFFD3DAE6);
        }
        int startY = y + 112;
        List<FeatureModule> visible = new ArrayList<>();
        if (NovaClient.MODULES != null) {
            for (FeatureModule module : NovaClient.MODULES.all()) {
                boolean cat = category.equals("All") || (category.equals("Visual") && module.category().equals("Visual")) || (category.equals("Quality") && module.category().equals("Quality of Life")) || (category.equals("Performance") && module.category().equals("Performance"));
                String q = search.toLowerCase(Locale.ROOT);
                boolean matches = q.isBlank() || module.name().toLowerCase(Locale.ROOT).contains(q) || module.description().toLowerCase(Locale.ROOT).contains(q);
                if (cat && matches) visible.add(module);
            }
        }
        int visibleRows = Math.max(1, (h - 122) / 62);
        for (int i = 0; i < Math.min(visible.size(), visibleRows); i++) {
            FeatureModule module = visible.get(i);
            int ry = startY + i * 62;
            ctx.fill(x, ry, x + w, ry + 54, 0xFF1B212D);
            ctx.fill(x, ry, x + 3, ry + 54, module.isEnabled() ? accentColor() : 0xFF3A4353);
            ctx.drawTextWithShadow(textRenderer, module.name(), x + 13, ry + 9, 0xFFFFFFFF);
            ctx.drawTextWithShadow(textRenderer, module.description(), x + 13, ry + 23, 0xFF9EABBD);
            String key = NovaClient.KEYBINDS == null ? "Unbound" : NovaClient.keyName(NovaClient.KEYBINDS.keyCode(module.id()));
            ctx.drawTextWithShadow(textRenderer, "Key: " + key, x + 13, ry + 38, 0xFF74839A);
            drawButton(ctx, x + w - 74, ry + 14, 63, 26, module.isEnabled() ? "ON" : "OFF", module.isEnabled() ? accentColor() : 0xFF333B4A, module.isEnabled() ? 0xFF101620 : 0xFFE4EAF2);
        }
        if (visible.isEmpty()) ctx.drawTextWithShadow(textRenderer, "No modules match that search.", x + 2, startY + 15, 0xFF9EABBD);
        if (visible.size() > visibleRows) ctx.drawTextWithShadow(textRenderer, "More features are below this view; narrow your search to find them.", x, y + h - 14, 0xFF7C8AA0);
    }

    private void renderHudPage(DrawContext ctx, int x, int y, int w, int h) {
        ctx.drawTextWithShadow(textRenderer, "HUD Editor", x, y, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Drag widgets to position them; style and resize each one.", x, y + 17, 0xFF98A5B8);
        card(ctx, x, y + 43, w, 82, "LIVE HUD", "" + NovaClient.CONFIG.hud.values().stream().filter(e -> e.enabled).count() + " enabled widgets", "FPS, ping, coordinates, movement, armor, effects and more", accentColor());
        drawButton(ctx, x, y + 139, 172, 32, "Open HUD Editor", accentColor(), 0xFF101620);
        drawButton(ctx, x + 183, y + 139, 142, 32, "Toggle Grid", 0xFF252D3B, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Available widgets", x, y + 190, 0xFFFFFFFF);
        int i = 0;
        for (var entry : NovaClient.CONFIG.hud.entrySet()) {
            if (i >= 7) break;
            int cy = y + 213 + i * 25;
            ctx.drawTextWithShadow(textRenderer, titleCase(entry.getKey()), x + 2, cy, entry.getValue().enabled ? 0xFFDCE5F3 : 0xFF758198);
            ctx.drawTextWithShadow(textRenderer, entry.getValue().enabled ? "ENABLED" : "DISABLED", x + w - 68, cy, entry.getValue().enabled ? 0xFF8FE7C2 : 0xFF7B8494);
            i++;
        }
    }


    private void renderPerformance(DrawContext ctx, int x, int y, int w, int h) {
        ctx.drawTextWithShadow(textRenderer, "Performance Center", x, y, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Reversible vanilla video settings; your original values are saved locally.", x, y + 17, 0xFF98A5B8);
        String preset = NovaClient.CONFIG.performancePreset;
        String presetLabel = switch (preset) {
            case "MAX_FPS" -> "MAX FPS ACTIVE";
            case "BALANCED" -> "BALANCED ACTIVE";
            default -> "CUSTOM / ORIGINAL SETTINGS";
        };
        card(ctx, x, y + 37, w, 54, "CURRENT PROFILE", presetLabel, PerformanceManager.currentSummary(MinecraftClient.getInstance()), accentColor());
        drawButton(ctx, x, y + 99, w, 30, "Apply MAX FPS preset", 0xFF2B3D4D, 0xFF8FE7FF);
        ctx.drawTextWithShadow(textRenderer, "Lower render/simulation distance, Fast graphics, minimal particles,", x + 2, y + 135, 0xFFB6C2D4);
        ctx.drawTextWithShadow(textRenderer, "clouds off and no entity shadows.", x + 2, y + 149, 0xFFB6C2D4);
        drawButton(ctx, x, y + 172, w, 30, "Apply BALANCED preset", 0xFF252D3B, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Moderate distance/effects reductions for better visual balance.", x + 2, y + 208, 0xFFB6C2D4);
        drawButton(ctx, x, y + 231, w, 26, "Restore previous video settings", 0xFF4A2B39, 0xFFFFDCE5);
        if (h >= 320) {
            ctx.drawTextWithShadow(textRenderer, "DLSS: not built in", x, y + 273, 0xFF8FE7C2);
            ctx.drawTextWithShadow(textRenderer, "True DLSS needs compatible NVIDIA hardware and renderer integration.", x, y + 291, 0xFF9AA8BC);
            ctx.drawTextWithShadow(textRenderer, "NOVA does not mislabel ordinary settings changes as DLSS.", x, y + 306, 0xFF9AA8BC);
        }
    }

    private void renderSettings(DrawContext ctx, int x, int y, int w, int h) {
        ctx.drawTextWithShadow(textRenderer, "Settings", x, y, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Preferences are stored locally in config/nova-client.json.", x, y + 17, 0xFF98A5B8);
        settingRow(ctx, x, y + 44, w, "Accent color", NovaClient.CONFIG.accentColor, "accent");
        settingRow(ctx, x, y + 80, w, "HUD in menus", NovaClient.CONFIG.showHudInMenus ? "Enabled" : "Disabled", "menus");
        settingRow(ctx, x, y + 116, w, "Editor alignment grid", NovaClient.CONFIG.editorGrid ? "Enabled" : "Disabled", "grid");
        String menuKey = NovaClient.KEYBINDS == null ? "Right Shift" : NovaClient.keyName(NovaClient.KEYBINDS.keyCode("menu"));
        if (NovaClient.KEYBINDS != null && NovaClient.KEYBINDS.hasConflict("menu")) menuKey += "  !";
        settingRow(ctx, x, y + 152, w, "Open menu key", menuKey, "rebind:menu");
        int ky = y + 188;
        if (NovaClient.MODULES != null) {
            for (FeatureModule module : NovaClient.MODULES.all()) {
                String keyName = NovaClient.KEYBINDS == null ? "Unbound" : NovaClient.keyName(NovaClient.KEYBINDS.keyCode(module.id()));
                if (NovaClient.KEYBINDS != null && NovaClient.KEYBINDS.hasConflict(module.id())) keyName += "  !";
                settingRow(ctx, x, ky, w, module.name() + " key", keyName, "rebind:" + module.id());
                ky += 34;
            }
        }
        int bottom = y + Math.min(h - 38, 330);
        drawButton(ctx, x, bottom, 124, 28, "Export JSON", 0xFF252D3B, 0xFFFFFFFF);
        drawButton(ctx, x + 133, bottom, 124, 28, "Import JSON", 0xFF252D3B, 0xFFFFFFFF);
        drawButton(ctx, x + 266, bottom, 124, 28, "Reset All", 0xFF4A2B39, 0xFFFFDCE5);
    }

    private void renderAbout(DrawContext ctx, int x, int y, int w, int h) {
        ctx.drawTextWithShadow(textRenderer, "About NOVA Client", x, y, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "A free, local-first Fabric client utility for Minecraft Java 1.20.1.", x, y + 18, 0xFF98A5B8);
        card(ctx, x, y + 52, w, 90, "PROJECT", "Version 1.1.0  •  Fabric Loader  •  Java 17", "MIT licensed. No account, telemetry, ads, or paid API.", accentColor());
        ctx.drawTextWithShadow(textRenderer, "Implemented: Fullbright, Zoom, Toggle Sprint, configurable HUD,", x, y + 166, 0xFFD7DEEA);
        ctx.drawTextWithShadow(textRenderer, "keybind editor, frame-time monitor, performance profiles, local config and CI.", x, y + 182, 0xFFD7DEEA);
        ctx.drawTextWithShadow(textRenderer, "Repository / issue tracker: add your published GitHub URL in README.md.", x, y + 218, 0xFF98A5B8);
        ctx.drawTextWithShadow(textRenderer, "Arabic translations are included; shaping can depend on Minecraft's font renderer.", x, y + 250, 0xFF7E8CA1);
    }

    private void settingRow(DrawContext ctx, int x, int y, int w, String label, String value, String action) {
        ctx.fill(x, y, x + w, y + 30, 0xFF1B212D);
        ctx.drawTextWithShadow(textRenderer, label, x + 10, y + 10, 0xFFE3EAF4);
        drawButton(ctx, x + w - 145, y + 3, 135, 24, value, 0xFF303949, 0xFFEAF0F8);
    }

    private void card(DrawContext ctx, int x, int y, int w, int h, String title, String main, String sub, int accent) {
        ctx.fill(x, y, x + w, y + h, 0xFF1B212D);
        ctx.fill(x, y, x + 3, y + h, accent);
        ctx.drawTextWithShadow(textRenderer, title, x + 13, y + 11, 0xFF8E9CB2);
        ctx.drawTextWithShadow(textRenderer, main, x + 13, y + 30, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, sub, x + 13, y + h - 18, 0xFF8E9CB2);
    }

    private void drawButton(DrawContext ctx, int x, int y, int w, int h, String label, int bg, int fg) {
        ctx.fill(x, y, x + w, y + h, bg);
        int tw = textRenderer.getWidth(label);
        ctx.drawTextWithShadow(textRenderer, label, x + (w - tw) / 2, y + (h - 8) / 2, fg);
    }

    private int panelWidth() { return Math.min(900, Math.max(400, width - 20)); }
    private int panelHeight() { return Math.min(570, Math.max(360, height - 20)); }
    private int panelX() { return (width - panelWidth()) / 2; }
    private int panelY() { return (height - panelHeight()) / 2; }
    private int accentColor() { return color(NovaClient.CONFIG == null ? "#55D6FF" : NovaClient.CONFIG.accentColor); }

    private static int color(String value) {
        try { return 0xFF000000 | Integer.parseInt(value.substring(1), 16); }
        catch (Exception ignored) { return 0xFF55D6FF; }
    }

    private String titleCase(String id) {
        String[] words = id.split("_");
        StringBuilder out = new StringBuilder();
        for (String word : words) {
            if (!out.isEmpty()) out.append(' ');
            out.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return out.toString();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        int x = panelX(), y = panelY(), w = panelWidth(), h = panelHeight();
        if (mouseX >= x + w - 36 && mouseX <= x + w - 8 && mouseY >= y + 8 && mouseY <= y + 38) {
            close(); return true;
        }
        String[] nav = {"Dashboard", "Modules", "HUD Editor", "Performance", "Settings", "About"};
        for (int i = 0; i < nav.length; i++) {
            int ny = y + 76 + i * 42;
            if (inside(mouseX, mouseY, x + 10, ny - 5, 144, 32)) {
                page = nav[i];
                clearChildren();
                init();
                return true;
            }
        }
        int cx = x + 184, cy = y + 75, cw = w - 204, ch = h - 100;
        if (page.equals("Dashboard")) {
            if (inside(mouseX, mouseY, cx, cy + 241, 132, 30)) setPage("Modules");
            else if (inside(mouseX, mouseY, cx + 142, cy + 241, 132, 30)) client.setScreen(new HudEditorScreen(this));
            else if (inside(mouseX, mouseY, cx + 284, cy + 241, 132, 30)) setPage("Settings");
            return true;
        }
        if (page.equals("Modules")) {
            String[] cats = {"All", "Visual", "Quality", "Perf"};
            int categoryWidth = Math.min(74, Math.max(42, (cw - 12) / cats.length));
            for (int i = 0; i < cats.length; i++) {
                if (inside(mouseX, mouseY, cx + i * (categoryWidth + 4), cy + 42, categoryWidth, 24)) {
                    category = cats[i].equals("Perf") ? "Performance" : cats[i];
                    return true;
                }
            }
            if (mouseY >= cy + 112 && mouseY <= cy + 112 + Math.max(1, (ch - 122) / 62) * 62 && NovaClient.MODULES != null) {
                List<FeatureModule> visible = new ArrayList<>();
                for (FeatureModule module : NovaClient.MODULES.all()) {
                    boolean cat = category.equals("All") || (category.equals("Visual") && module.category().equals("Visual")) || (category.equals("Quality") && module.category().equals("Quality of Life")) || (category.equals("Performance") && module.category().equals("Performance"));
                    String q = search.toLowerCase(Locale.ROOT);
                    boolean matches = q.isBlank() || module.name().toLowerCase(Locale.ROOT).contains(q) || module.description().toLowerCase(Locale.ROOT).contains(q);
                    if (cat && matches) visible.add(module);
                }
                int index = (int) ((mouseY - (cy + 112)) / 62);
                if (index >= 0 && index < visible.size()) NovaClient.toggleModule(visible.get(index).id());
                return true;
            }
        }
        if (page.equals("HUD Editor")) {
            if (inside(mouseX, mouseY, cx, cy + 139, 172, 32)) client.setScreen(new HudEditorScreen(this));
            else if (inside(mouseX, mouseY, cx + 183, cy + 139, 142, 32)) { NovaClient.CONFIG.editorGrid = !NovaClient.CONFIG.editorGrid; NovaClient.saveConfig(); }
            else {
                int i = 0;
                for (var entry : NovaClient.CONFIG.hud.entrySet()) {
                    if (i >= 7) break;
                    if (inside(mouseX, mouseY, cx, cy + 207 + i * 25, cw, 24)) { entry.getValue().enabled = !entry.getValue().enabled; NovaClient.saveConfig(); return true; }
                    i++;
                }
            }
            return true;
        }
        if (page.equals("Performance")) {
            if (inside(mouseX, mouseY, cx, cy + 99, cw, 30)) {
                PerformanceManager.applyPreset(client, "MAX_FPS");
                notice = "MAX FPS preset applied. Restore previous settings any time.";
                return true;
            }
            if (inside(mouseX, mouseY, cx, cy + 172, cw, 30)) {
                PerformanceManager.applyPreset(client, "BALANCED");
                notice = "Balanced preset applied.";
                return true;
            }
            if (inside(mouseX, mouseY, cx, cy + 231, cw, 26)) {
                PerformanceManager.restoreOriginal(client);
                notice = "Previous video settings restored.";
                return true;
            }
            return true;
        }
        if (page.equals("Settings")) {
            if (inside(mouseX, mouseY, cx + cw - 145, cy + 47, 135, 24)) {
                String[] palette = {"#55D6FF", "#B78CFF", "#72E6A6", "#FFB86B", "#FF7893"};
                int index = 0;
                for (int i = 0; i < palette.length; i++) if (palette[i].equalsIgnoreCase(NovaClient.CONFIG.accentColor)) index = i;
                NovaClient.CONFIG.accentColor = palette[(index + 1) % palette.length];
                NovaClient.saveConfig(); return true;
            }
            if (inside(mouseX, mouseY, cx + cw - 145, cy + 83, 135, 24)) { NovaClient.CONFIG.showHudInMenus = !NovaClient.CONFIG.showHudInMenus; NovaClient.saveConfig(); return true; }
            if (inside(mouseX, mouseY, cx + cw - 145, cy + 119, 135, 24)) { NovaClient.CONFIG.editorGrid = !NovaClient.CONFIG.editorGrid; NovaClient.saveConfig(); return true; }
            if (inside(mouseX, mouseY, cx + cw - 145, cy + 155, 135, 24)) { beginRebind("menu"); return true; }
            int ky = cy + 188;
            if (NovaClient.MODULES != null) for (FeatureModule module : NovaClient.MODULES.all()) {
                if (inside(mouseX, mouseY, cx + cw - 145, ky + 3, 135, 24)) { beginRebind(module.id()); return true; }
                ky += 34;
            }
            int bottom = cy + Math.min(ch - 38, 330);
            if (inside(mouseX, mouseY, cx, bottom, 124, 28)) {
                try { var path = ConfigManager.exportConfig(NovaClient.CONFIG); notice = "Exported: " + path.getFileName(); }
                catch (IOException exception) { notice = "Export failed; see latest.log."; NovaClient.LOGGER.error("Could not export settings", exception); }
                return true;
            }
            if (inside(mouseX, mouseY, cx + 133, bottom, 124, 28)) {
                try { NovaClient.applyConfig(ConfigManager.importConfig()); notice = "Imported nova-client-export.json"; }
                catch (Exception exception) { notice = "Import failed: create or repair nova-client-export.json"; NovaClient.LOGGER.warn("Could not import settings", exception); }
                return true;
            }
            if (inside(mouseX, mouseY, cx + 266, bottom, 124, 28)) { NovaClient.resetConfiguration(); notice = "All settings reset to defaults."; return true; }
        }
        return true;
    }

    private void setPage(String next) {
        page = next;
        clearChildren();
        init();
    }

    private void beginRebind(String id) {
        rebindingId = id;
        notice = "Waiting for key input…";
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (rebindingId != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                rebindingId = null;
                notice = "Key assignment cancelled.";
            } else {
                int chosen = (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) ? GLFW.GLFW_KEY_UNKNOWN : keyCode;
                NovaClient.KEYBINDS.setKeyCode(rebindingId, chosen, NovaClient.CONFIG);
                NovaClient.saveConfig();
                notice = NovaClient.KEYBINDS.hasConflict(rebindingId)
                        ? "Key saved with a conflict; review the ! marker in Settings."
                        : "Key saved: " + NovaClient.keyName(chosen);
                rebindingId = null;
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }

    private static boolean inside(double px, double py, int x, int y, int w, int h) {
        return px >= x && py >= y && px < x + w && py < y + h;
    }
}
