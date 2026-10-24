package com.novaclient.hud;

import com.novaclient.NovaClient;
import com.novaclient.config.HudElementConfig;
import com.novaclient.config.NovaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import org.lwjgl.glfw.GLFW;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Lightweight cached HUD renderer shared by normal play and the HUD editor. */
public final class HudRenderer {
    private static final Map<String, List<String>> CACHED_LINES = new LinkedHashMap<>();
    private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static final Deque<Long> RIGHT_CLICKS = new ArrayDeque<>();
    private static int tickCounter;
    private static final double[] FRAME_TIME_SAMPLES = new double[120];
    private static int frameTimeCount;
    private static int frameTimeIndex;
    private static long lastFrameNanos;

    private HudRenderer() { }

    /** Called once from the HUD render callback; stores only a small rolling frame-time sample. */
    public static void recordFrame() {
        long now = System.nanoTime();
        if (lastFrameNanos != 0L) {
            long elapsed = now - lastFrameNanos;
            if (elapsed > 0L && elapsed < 1_000_000_000L) {
                FRAME_TIME_SAMPLES[frameTimeIndex] = elapsed / 1_000_000.0;
                frameTimeIndex = (frameTimeIndex + 1) % FRAME_TIME_SAMPLES.length;
                frameTimeCount = Math.min(frameTimeCount + 1, FRAME_TIME_SAMPLES.length);
            }
        }
        lastFrameNanos = now;
    }

    public static void tick(MinecraftClient client) {
        tickCounter++;
        if (tickCounter % 5 == 0) refresh(client);
    }

    public static void refresh(MinecraftClient client) {
        if (NovaClient.CONFIG == null) return;
        long now = System.currentTimeMillis();
        trimClicks(LEFT_CLICKS, now);
        trimClicks(RIGHT_CLICKS, now);
        CACHED_LINES.clear();
        CACHED_LINES.put("fps", List.of("FPS  " + MinecraftClient.getInstance().getCurrentFps()));
        if (frameTimeCount > 0) {
            double[] sorted = new double[frameTimeCount];
            System.arraycopy(FRAME_TIME_SAMPLES, 0, sorted, 0, frameTimeCount);
            java.util.Arrays.sort(sorted);
            double sum = 0.0;
            for (double sample : sorted) sum += sample;
            double average = sum / sorted.length;
            double p95 = sorted[Math.max(0, (int) Math.ceil(sorted.length * 0.95) - 1)];
            CACHED_LINES.put("frametime", List.of(String.format(java.util.Locale.ROOT, "Frame  %.2f ms avg", average), String.format(java.util.Locale.ROOT, "95%%  %.2f ms", p95)));
        } else {
            CACHED_LINES.put("frametime", List.of("Frame time  collecting…"));
        }

        String ping = "Ping  —";
        if (client != null && client.player != null && client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
            if (entry != null) ping = "Ping  " + entry.getLatency() + " ms";
            else ping = "Ping  —";
        }
        CACHED_LINES.put("ping", List.of(ping));

        if (client == null || client.player == null || client.world == null) {
            CACHED_LINES.put("coordinates", List.of("XYZ  —"));
            CACHED_LINES.put("direction", List.of("Facing  —"));
            CACHED_LINES.put("biome", List.of("Biome  —"));
            CACHED_LINES.put("keystrokes", List.of("W  A  S  D", "SPACE  SPRINT"));
            CACHED_LINES.put("player_status", List.of("Health  —", "Food  —"));
            CACHED_LINES.put("speed", List.of("Speed  —"));
            CACHED_LINES.put("armor", List.of("Armor  —"));
            CACHED_LINES.put("effects", List.of("Effects  —"));
        } else {
            var player = client.player;
            CACHED_LINES.put("coordinates", List.of(String.format(java.util.Locale.ROOT, "XYZ  %.1f / %.1f / %.1f", player.getX(), player.getY(), player.getZ())));
            CACHED_LINES.put("direction", List.of("Facing  " + player.getHorizontalFacing().asString().toUpperCase(java.util.Locale.ROOT)));
            String biomeName = "unknown";
            try {
                biomeName = client.world.getBiome(player.getBlockPos()).getKey().map(key -> key.getValue().getPath()).orElse("unknown");
            } catch (Exception ignored) { /* Registry information can be unavailable during world transitions. */ }
            CACHED_LINES.put("biome", List.of("Biome  " + biomeName.replace('_', ' ')));

            CACHED_LINES.put("keystrokes", List.of(
                    (client.options.forwardKey.isPressed() ? "[W]" : " W ") + "  " + (client.options.leftKey.isPressed() ? "[A]" : " A ") + "  " + (client.options.backKey.isPressed() ? "[S]" : " S ") + "  " + (client.options.rightKey.isPressed() ? "[D]" : " D "),
                    (client.options.jumpKey.isPressed() ? "[SPACE]" : " SPACE ") + "  " + (client.options.sprintKey.isPressed() ? "[SPRINT]" : " SPRINT ")));
            CACHED_LINES.put("player_status", List.of(String.format(java.util.Locale.ROOT, "Health  %.1f / %.1f", player.getHealth(), player.getMaxHealth()), "Food  " + player.getHungerManager().getFoodLevel()));
            double horizontal = Math.sqrt(player.getVelocity().x * player.getVelocity().x + player.getVelocity().z * player.getVelocity().z) * 20.0;
            CACHED_LINES.put("speed", List.of(String.format(java.util.Locale.ROOT, "Speed  %.2f blocks/s", horizontal)));

            List<String> armorLines = new ArrayList<>();
            String[] armorSlots = {"Boots", "Leggings", "Chest", "Helmet"};
            int index = 0;
            for (ItemStack stack : player.getInventory().armor) {
                if (!stack.isEmpty() && index < armorSlots.length) {
                    String durability = stack.isDamageable() ? (stack.getMaxDamage() - stack.getDamage()) + "/" + stack.getMaxDamage() : "OK";
                    armorLines.add(armorSlots[index] + "  " + durability);
                }
                index++;
            }
            if (armorLines.isEmpty()) armorLines.add("Armor  —");
            CACHED_LINES.put("armor", armorLines);

            List<String> effects = new ArrayList<>();
            for (StatusEffectInstance effect : player.getStatusEffects()) {
                effects.add(effect.getEffectType().getName().getString() + " " + (effect.getAmplifier() + 1) + "  " + Math.max(0, effect.getDuration() / 20) + "s");
                if (effects.size() >= 5) break;
            }
            if (effects.isEmpty()) effects.add("Effects  None");
            CACHED_LINES.put("effects", effects);
        }
        CACHED_LINES.put("cps", List.of("CPS  L " + LEFT_CLICKS.size() + "  R " + RIGHT_CLICKS.size()));
        CACHED_LINES.put("clock", List.of("Local time  " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        Runtime runtime = Runtime.getRuntime();
        long usedMiB = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
        long maxMiB = runtime.maxMemory() / (1024L * 1024L);
        CACHED_LINES.put("memory", List.of("Memory  " + usedMiB + " / " + maxMiB + " MiB"));
    }

    public static void recordClick(int button) {
        long now = System.currentTimeMillis();
        Deque<Long> target = button == GLFW.GLFW_MOUSE_BUTTON_LEFT ? LEFT_CLICKS : RIGHT_CLICKS;
        trimClicks(target, now);
        target.addLast(now);
        while (target.size() > 30) target.removeFirst();
    }

    private static void trimClicks(Deque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) clicks.removeFirst();
    }

    public static Map<String, List<String>> lines() {
        return java.util.Collections.unmodifiableMap(CACHED_LINES);
    }

    public static void render(DrawContext context, MinecraftClient client, boolean editing) {
        if (NovaClient.CONFIG == null || client == null) return;
        NovaConfig config = NovaClient.CONFIG;
        if (!editing && client.world == null && !config.showHudInMenus
                && config.hud.values().stream().noneMatch(element -> element.showInMenus)) return;
        for (Map.Entry<String, HudElementConfig> entry : config.hud.entrySet()) {
            String id = entry.getKey();
            HudElementConfig element = entry.getValue();
            if (!element.enabled && !editing) continue;
            if (!editing && client.world == null && !config.showHudInMenus && !element.showInMenus) continue;
            List<String> lines = displayLines(id);
            drawElement(context, client, element, lines, editing);
        }
    }

    public static HudBounds bounds(MinecraftClient client, String id) {
        HudElementConfig cfg = NovaClient.CONFIG == null ? null : NovaClient.CONFIG.hud.get(id);
        if (cfg == null || client == null) return new HudBounds(0, 0, 0, 0);
        List<String> lines = displayLines(id);
        int width = 0;
        for (String line : lines) width = Math.max(width, client.textRenderer.getWidth(line));
        int height = Math.max(1, lines.size()) * 10 + 8;
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int widgetWidth = Math.max(20, Math.round((width + 12) * cfg.scale));
        int widgetHeight = Math.max(16, Math.round(height * cfg.scale));
        int x = Math.max(0, Math.min(Math.round(cfg.x * screenWidth), Math.max(0, screenWidth - widgetWidth)));
        int y = Math.max(0, Math.min(Math.round(cfg.y * screenHeight), Math.max(0, screenHeight - widgetHeight)));
        return new HudBounds(x, y, widgetWidth, widgetHeight);
    }

    private static List<String> displayLines(String id) {
        List<String> lines = CACHED_LINES.get(id);
        if (lines != null && !lines.isEmpty()) return lines;
        return switch (id) {
            case "fps" -> List.of("FPS  144");
            case "ping" -> List.of("Ping  24 ms");
            case "coordinates" -> List.of("XYZ  120.0 / 64.0 / -32.0");
            case "direction" -> List.of("Facing  NORTH");
            case "biome" -> List.of("Biome  plains");
            case "keystrokes" -> List.of(" W   A   S   D ", " SPACE   SPRINT ");
            case "cps" -> List.of("CPS  L 0  R 0");
            case "armor" -> List.of("Armor  —");
            case "effects" -> List.of("Effects  None");
            case "clock" -> List.of("Local time  12:00:00");
            case "player_status" -> List.of("Health  20.0 / 20.0", "Food  20");
            case "speed" -> List.of("Speed  0.00 blocks/s");
            case "memory" -> List.of("Memory  512 / 2048 MiB");
            case "frametime" -> List.of("Frame  6.94 ms avg", "95%  8.10 ms");
            default -> List.of(id);
        };
    }

    private static void drawElement(DrawContext context, MinecraftClient client, HudElementConfig cfg, List<String> lines, boolean editing) {
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int baseWidth = 0;
        for (String line : lines) baseWidth = Math.max(baseWidth, client.textRenderer.getWidth(line));
        int width = Math.max(20, baseWidth + 12);
        int height = Math.max(16, lines.size() * 10 + 8);
        float scale = cfg.scale;
        int scaledWidth = Math.max(20, Math.round(width * scale));
        int scaledHeight = Math.max(16, Math.round(height * scale));
        int x = Math.max(0, Math.min(Math.round(cfg.x * screenWidth), Math.max(0, screenWidth - scaledWidth)));
        int y = Math.max(0, Math.min(Math.round(cfg.y * screenHeight), Math.max(0, screenHeight - scaledHeight)));
        int opacity = Math.max(24, Math.min(255, Math.round(cfg.opacity * 255.0f * (cfg.enabled ? 1.0f : 0.35f))));
        int textRgb = parseColor(cfg.textColor, 0xFFFFFF);
        int bgRgb = parseColor(cfg.backgroundColor, 0x151923);
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0.0f);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.fill(0, 0, width, height, (opacity * 3 / 5 << 24) | bgRgb);
        for (int line = 0; line < lines.size(); line++) {
            String text = lines.get(line);
            int lineWidth = client.textRenderer.getWidth(text);
            int textX = switch (cfg.alignment) {
                case "CENTER" -> 6 + (baseWidth - lineWidth) / 2;
                case "RIGHT" -> 6 + (baseWidth - lineWidth);
                default -> 6;
            };
            context.drawTextWithShadow(client.textRenderer, text, textX, 4 + line * 10, (opacity << 24) | textRgb);
        }
        context.getMatrices().pop();
    }

    public static void drawSelectionBorder(DrawContext context, HudBounds bounds, int color) {
        int x = bounds.x(), y = bounds.y(), w = bounds.width(), h = bounds.height();
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static int parseColor(String color, int fallback) {
        try {
            if (color == null) return fallback;
            String clean = color.startsWith("#") ? color.substring(1) : color;
            return Integer.parseInt(clean, 16) & 0xFFFFFF;
        } catch (RuntimeException ignored) { return fallback; }
    }

    public record HudBounds(int x, int y, int width, int height) {
        public boolean contains(double px, double py) { return px >= x && py >= y && px < x + width && py < y + height; }
    }
}
