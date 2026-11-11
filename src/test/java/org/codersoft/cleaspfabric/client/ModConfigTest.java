package org.codersoft.cleaspfabric.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModConfigTest {

    @TempDir
    Path tempDir;

    private Path configFile;

    @BeforeEach
    void setUp() {
        configFile = tempDir.resolve("invistags.json");
        ModConfig.rememberState = true;
        ModConfig.showInvisibleSkin = false;
        ModConfig.showInvisibleNametag = false;
        ModConfig.showInvisibleMobs = false;
        ModConfig.showInvisibleGear = false;
        ModConfig.toastInvisiblePlayers = false;
    }

    @AfterEach
    void reset() {
        ModConfig.rememberState = true;
        ModConfig.showInvisibleSkin = false;
        ModConfig.showInvisibleNametag = false;
        ModConfig.showInvisibleMobs = false;
        ModConfig.showInvisibleGear = false;
        ModConfig.toastInvisiblePlayers = false;
    }

    @Test
    void saveWithoutRememberWritesOnlyFlag() throws Exception {
        ModConfig.rememberState = false;
        ModConfig.showInvisibleSkin = true;
        ModConfig.showInvisibleNametag = true;

        assertTrue(ModConfig.save(configFile));

        String json = Files.readString(configFile);
        assertTrue(json.contains("\"rememberState\": false"));
        assertFalse(json.contains("showInvisibleSkin"));
        assertFalse(json.contains("showInvisibleNametag"));
        assertFalse(json.contains("showInvisibleGear"));
        assertFalse(json.contains("toastInvisiblePlayers"));
    }

    @Test
    void saveWithRememberWritesValues() throws Exception {
        ModConfig.rememberState = true;
        ModConfig.showInvisibleSkin = false;
        ModConfig.showInvisibleNametag = true;
        ModConfig.showInvisibleGear = true;
        ModConfig.toastInvisiblePlayers = true;

        assertTrue(ModConfig.save(configFile));

        String json = Files.readString(configFile);
        assertTrue(json.contains("\"rememberState\": true"));
        assertTrue(json.contains("\"showInvisibleSkin\": false"));
        assertTrue(json.contains("\"showInvisibleNametag\": true"));
        assertTrue(json.contains("\"showInvisibleGear\": true"));
        assertTrue(json.contains("\"toastInvisiblePlayers\": true"));
    }

    @Test
    void loadWithoutRememberStartsOff() throws Exception {
        Files.writeString(configFile, "{\"rememberState\": false, \"showInvisibleSkin\": true, \"showInvisibleNametag\": true, \"showInvisibleMobs\": true, \"showInvisibleGear\": true, \"toastInvisiblePlayers\": true}");

        assertTrue(ModConfig.load(configFile));
        assertFalse(ModConfig.rememberState);
        assertFalse(ModConfig.showInvisibleSkin);
        assertFalse(ModConfig.showInvisibleNametag);
        assertFalse(ModConfig.showInvisibleMobs);
        assertFalse(ModConfig.showInvisibleGear);
        assertFalse(ModConfig.toastInvisiblePlayers);
    }

    @Test
    void loadWithRememberRestoresValues() {
        ModConfig.rememberState = true;
        ModConfig.showInvisibleSkin = false;
        ModConfig.showInvisibleNametag = false;
        ModConfig.showInvisibleMobs = true;
        ModConfig.showInvisibleGear = true;
        ModConfig.toastInvisiblePlayers = true;
        assertTrue(ModConfig.save(configFile));

        ModConfig.rememberState = false;
        ModConfig.showInvisibleSkin = true;
        ModConfig.showInvisibleNametag = true;
        ModConfig.showInvisibleMobs = false;
        ModConfig.showInvisibleGear = false;
        ModConfig.toastInvisiblePlayers = false;

        assertTrue(ModConfig.load(configFile));
        assertTrue(ModConfig.rememberState);
        assertFalse(ModConfig.showInvisibleSkin);
        assertFalse(ModConfig.showInvisibleNametag);
        assertTrue(ModConfig.showInvisibleMobs);
        assertTrue(ModConfig.showInvisibleGear);
        assertTrue(ModConfig.toastInvisiblePlayers);
    }

    @Test
    void missingFileIsCreatedWithDefaults() throws Exception {
        assertFalse(Files.exists(configFile));

        assertTrue(ModConfig.load(configFile));

        assertTrue(Files.exists(configFile));
        assertTrue(ModConfig.rememberState);
        assertFalse(ModConfig.showInvisibleSkin);
        assertFalse(ModConfig.showInvisibleNametag);
        assertFalse(ModConfig.showInvisibleMobs);
        assertFalse(ModConfig.showInvisibleGear);
        assertFalse(ModConfig.toastInvisiblePlayers);
    }

    @Test
    void malformedJsonKeepsDefaults() throws Exception {
        Files.writeString(configFile, "{ this is not json");

        assertFalse(ModConfig.load(configFile));
        assertTrue(ModConfig.rememberState);
        assertFalse(ModConfig.showInvisibleSkin);
        assertFalse(ModConfig.showInvisibleNametag);
        assertFalse(ModConfig.showInvisibleMobs);
    }

    @Test
    void partialFileKeepsUnspecifiedDefaults() throws Exception {
        Files.writeString(configFile, "{\"rememberState\": true, \"showInvisibleSkin\": true}");

        assertTrue(ModConfig.load(configFile));
        assertTrue(ModConfig.rememberState);
        assertTrue(ModConfig.showInvisibleSkin);
        assertFalse(ModConfig.showInvisibleNametag);
        assertFalse(ModConfig.showInvisibleMobs);
        assertFalse(ModConfig.showInvisibleGear);
        assertFalse(ModConfig.toastInvisiblePlayers);
    }

    @Test
    void gearAndToastRoundTrip() throws Exception {
        ModConfig.showInvisibleGear = true;
        ModConfig.toastInvisiblePlayers = true;

        assertTrue(ModConfig.save(configFile));
        ModConfig.showInvisibleGear = false;
        ModConfig.toastInvisiblePlayers = false;

        assertTrue(ModConfig.load(configFile));
        assertTrue(ModConfig.showInvisibleGear);
        assertTrue(ModConfig.toastInvisiblePlayers);
    }

    @Test
    void unknownKeysAreIgnored() throws Exception {
        Files.writeString(configFile, "{\"rememberState\": true, \"showInvisibleNametag\": true, \"foo\": 123}");

        assertTrue(ModConfig.load(configFile));
        assertTrue(ModConfig.rememberState);
        assertFalse(ModConfig.showInvisibleSkin);
        assertTrue(ModConfig.showInvisibleNametag);
    }

    @Test
    void wrongValueTypeIsIgnored() throws Exception {
        Files.writeString(configFile, "{\"rememberState\": \"yes\", \"showInvisibleSkin\": \"yes\"}");

        assertTrue(ModConfig.load(configFile));
        assertTrue(ModConfig.rememberState);
        assertFalse(ModConfig.showInvisibleSkin);
        assertFalse(ModConfig.showInvisibleNametag);
    }
}
