package org.codersoft.cleaspfabric.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    public static boolean rememberState = true;
    public static boolean showInvisibleSkin = false;
    public static boolean showInvisibleNametag = false;
    public static boolean showInvisibleMobs = false;
    public static boolean showInvisibleGear = false;
    public static boolean toastInvisiblePlayers = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("invistags.json");
    }

    public static void load() {
        Path path = getConfigPath();
        if (load(path)) {
            CleaspfabricClient.LOGGER.info("Config loaded from {}", path);
        } else {
            CleaspfabricClient.LOGGER.warn("Could not read invistags.json, using defaults");
        }
    }

    public static void save() {
        if (!save(getConfigPath())) {
            CleaspfabricClient.LOGGER.warn("Could not save invistags.json");
        }
    }

    /**
     * Loads config from the given file, creating it with defaults if it does not exist.
     * When rememberState is off, saved feature toggles are ignored and features start off.
     *
     * @return true if the file was read or created successfully
     */
    public static boolean load(Path path) {
        if (!Files.exists(path)) {
            return save(path);
        }

        boolean remember = rememberState;
        boolean skin = false;
        boolean nametag = false;
        boolean mobs = false;
        boolean gear = false;
        boolean toast = false;
        try {
            String json = Files.readString(path);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonElement rememberElement = obj.get("rememberState");
            if (isBoolean(rememberElement)) {
                remember = rememberElement.getAsBoolean();
            }
            if (remember) {
                JsonElement skinElement = obj.get("showInvisibleSkin");
                if (isBoolean(skinElement)) {
                    skin = skinElement.getAsBoolean();
                }
                JsonElement nametagElement = obj.get("showInvisibleNametag");
                if (isBoolean(nametagElement)) {
                    nametag = nametagElement.getAsBoolean();
                }
                JsonElement mobsElement = obj.get("showInvisibleMobs");
                if (isBoolean(mobsElement)) {
                    mobs = mobsElement.getAsBoolean();
                }
                JsonElement gearElement = obj.get("showInvisibleGear");
                if (isBoolean(gearElement)) {
                    gear = gearElement.getAsBoolean();
                }
                JsonElement toastElement = obj.get("toastInvisiblePlayers");
                if (isBoolean(toastElement)) {
                    toast = toastElement.getAsBoolean();
                }
            }
        } catch (Exception e) {
            return false;
        }

        rememberState = remember;
        showInvisibleSkin = skin;
        showInvisibleNametag = nametag;
        showInvisibleMobs = mobs;
        showInvisibleGear = gear;
        toastInvisiblePlayers = toast;
        return true;
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
            obj.addProperty("rememberState", rememberState);
            if (rememberState) {
                obj.addProperty("showInvisibleSkin", showInvisibleSkin);
                obj.addProperty("showInvisibleNametag", showInvisibleNametag);
                obj.addProperty("showInvisibleMobs", showInvisibleMobs);
                obj.addProperty("showInvisibleGear", showInvisibleGear);
                obj.addProperty("toastInvisiblePlayers", toastInvisiblePlayers);
            }
            Files.writeString(path, GSON.toJson(obj) + "\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
