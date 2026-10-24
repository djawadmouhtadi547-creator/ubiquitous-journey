package com.novaclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.novaclient.NovaClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;

/** Atomic, local-only JSON configuration storage. */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final long MAX_CONFIG_BYTES = 1024L * 1024L;

    private ConfigManager() { }

    public static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("nova-client.json");
    }

    public static Path exportPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("nova-client-export.json");
    }

    public static NovaConfig load() {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                NovaConfig defaults = NovaConfig.defaults();
                save(defaults);
                return defaults;
            }
            NovaConfig config = readAndValidate(path);
            config.normalize();
            save(config);
            return config;
        } catch (Exception exception) {
            NovaClient.LOGGER.error("Could not load NOVA settings. The original file will be preserved if possible.", exception);
            preserveBrokenFile(path);
            NovaConfig defaults = NovaConfig.defaults();
            try { save(defaults); } catch (Exception saveError) {
                NovaClient.LOGGER.error("Could not write default NOVA settings.", saveError);
            }
            return defaults;
        }
    }

    public static void save(NovaConfig config) {
        if (config == null) return;
        Path target = configPath();
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            config.normalize();
            Files.createDirectories(target.getParent());
            Files.writeString(temporary, GSON.toJson(config));
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveNotSupported) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            NovaClient.LOGGER.error("Could not save NOVA settings to {}", target, exception);
        } finally {
            try { Files.deleteIfExists(temporary); } catch (IOException ignored) { /* Best-effort cleanup. */ }
        }
    }

    public static Path exportConfig(NovaConfig config) throws IOException {
        Path target = exportPath();
        Files.createDirectories(target.getParent());
        Files.writeString(target, GSON.toJson(config));
        return target;
    }

    public static NovaConfig importConfig() throws IOException {
        Path source = exportPath();
        if (!Files.exists(source)) throw new IOException("No export file found at " + source);
        NovaConfig config = readAndValidate(source);
        config.normalize();
        return config;
    }

    private static NovaConfig readAndValidate(Path path) throws IOException {
        long size = Files.size(path);
        if (size <= 0 || size > MAX_CONFIG_BYTES) throw new IOException("Settings file has an invalid size.");
        try (var reader = Files.newBufferedReader(path)) {
            NovaConfig parsed = GSON.fromJson(JsonParser.parseReader(reader), NovaConfig.class);
            if (parsed == null) throw new JsonParseException("Empty configuration");
            return parsed;
        } catch (JsonParseException exception) {
            throw new IOException("Settings file is not valid JSON.", exception);
        }
    }

    private static void preserveBrokenFile(Path path) {
        try {
            if (Files.exists(path)) {
                Path backup = path.resolveSibling(path.getFileName() + ".broken-" + Instant.now().toEpochMilli());
                Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
                NovaClient.LOGGER.warn("Preserved the invalid settings file as {}", backup.getFileName());
            }
        } catch (IOException exception) {
            NovaClient.LOGGER.error("Could not preserve invalid settings file at {}", path, exception);
        }
    }
}
