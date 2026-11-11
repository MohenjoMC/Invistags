package org.codersoft.cleaspfabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/**
 * Toggle values plus their JSON persistence. The set of toggles and their config keys
 * live in {@link Toggle}; this class only stores a value per toggle and reads/writes it.
 */
public final class ModConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("invisnametag");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<Toggle, Boolean> VALUES = new EnumMap<>(Toggle.class);

    static {
        resetToDefaults();
    }

    private ModConfig() {
    }

    /** Current value of a toggle. */
    public static boolean get(Toggle toggle) {
        return VALUES.get(toggle);
    }

    /** Sets a toggle without writing the file; call {@link #save()} to persist. */
    public static void set(Toggle toggle, boolean value) {
        VALUES.put(toggle, value);
    }

    private static void resetToDefaults() {
        for (Toggle toggle : Toggle.values()) {
            VALUES.put(toggle, toggle.defaultValue());
        }
    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("invistags.json");
    }

    /** Loads the config file from the Fabric config directory. */
    public static void load() {
        Path path = getConfigPath();
        if (load(path)) {
            LOGGER.info("Config loaded from {}", path);
        } else {
            LOGGER.warn("Could not read invistags.json, using defaults");
        }
    }

    /** Writes the config file in the Fabric config directory. */
    public static void save() {
        if (!save(getConfigPath())) {
            LOGGER.warn("Could not save invistags.json");
        }
    }

    /**
     * Loads config from the given file, creating it with the current values if it does not
     * exist. When rememberState is off, saved feature toggles are ignored and features start
     * off; the flag itself is always read.
     *
     * @return true if the file was read or created successfully
     */
    public static boolean load(Path path) {
        if (!Files.exists(path)) {
            return save(path);
        }

        boolean remember = get(Toggle.REMEMBER);
        Map<Toggle, Boolean> loaded = new EnumMap<>(Toggle.class);
        try {
            JsonObject obj = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            JsonElement rememberElement = obj.get(Toggle.REMEMBER.configKey());
            if (isBoolean(rememberElement)) {
                remember = rememberElement.getAsBoolean();
            }
            loaded.put(Toggle.REMEMBER, remember);
            for (Toggle toggle : Toggle.values()) {
                if (!toggle.persistedWhileRemembering()) {
                    continue;
                }
                loaded.put(toggle, readValue(obj, toggle, remember));
            }
        } catch (Exception e) {
            return false;
        }

        loaded.forEach(ModConfig::set);
        return true;
    }

    /**
     * Reads one feature toggle: remember-off means every feature starts disabled, a
     * missing or wrongly typed key falls back to the toggle's declared default.
     */
    static boolean readValue(JsonObject obj, Toggle toggle, boolean remember) {
        if (!remember) {
            return false;
        }
        JsonElement element = obj.get(toggle.configKey());
        return isBoolean(element) ? element.getAsBoolean() : toggle.defaultValue();
    }

    private static boolean isBoolean(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
    }

    /**
     * Writes the current settings to the given file, creating parent directories if needed.
     * Feature toggles are only written when rememberState is on.
     *
     * @return true if the file was written successfully
     */
    public static boolean save(Path path) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            JsonObject obj = new JsonObject();
            boolean remember = get(Toggle.REMEMBER);
            obj.addProperty(Toggle.REMEMBER.configKey(), remember);
            if (remember) {
                for (Toggle toggle : Toggle.values()) {
                    if (toggle.persistedWhileRemembering()) {
                        obj.addProperty(toggle.configKey(), get(toggle));
                    }
                }
            }
            Files.writeString(path, GSON.toJson(obj) + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
