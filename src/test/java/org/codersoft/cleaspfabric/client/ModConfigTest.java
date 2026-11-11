package org.codersoft.cleaspfabric.client;

import org.codersoft.cleaspfabric.Toggle;

import org.codersoft.cleaspfabric.ModConfig;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModConfigTest {

    @TempDir
    Path tempDir;

    private Path configFile;

    @BeforeEach
    void setUp() {
        configFile = tempDir.resolve("invistags.json");
        resetToDefaults();
    }

    @AfterEach
    void reset() {
        resetToDefaults();
    }

    private static void resetToDefaults() {
        for (Toggle toggle : Toggle.values()) {
            ModConfig.set(toggle, toggle.defaultValue());
        }
    }

    @Test
    void everyToggleRoundTripsThroughTheFile() {
        for (Toggle toggle : Toggle.values()) {
            boolean flipped = !toggle.defaultValue();

            resetToDefaults();
            ModConfig.set(toggle, flipped);
            assertTrue(ModConfig.save(configFile), toggle + ": save failed");

            resetToDefaults();
            assertTrue(ModConfig.load(configFile), toggle + ": load failed");
            assertEquals(flipped, ModConfig.get(toggle), toggle + ": non-default value did not round-trip");

            resetToDefaults();
            assertTrue(ModConfig.save(configFile));
            resetToDefaults();
            ModConfig.set(toggle, flipped);
            assertTrue(ModConfig.load(configFile));
            assertEquals(toggle.defaultValue(), ModConfig.get(toggle),
                toggle + ": default value did not round-trip");
        }
    }

    @Test
    void saveWithoutRememberWritesOnlyTheFlag() throws Exception {
        ModConfig.set(Toggle.REMEMBER, false);
        setAllFeatures(true);

        assertTrue(ModConfig.save(configFile));

        String json = Files.readString(configFile);
        assertTrue(json.contains("\"rememberState\": false"));
        for (Toggle toggle : Toggle.values()) {
            if (toggle.persistedWhileRemembering()) {
                assertFalse(json.contains(toggle.configKey()), toggle + " must not be written");
            }
        }
    }

    @Test
    void loadWithoutRememberStartsEveryFeatureOff() throws Exception {
        StringBuilder json = new StringBuilder("{\"rememberState\": false");
        for (Toggle toggle : Toggle.values()) {
            if (toggle.persistedWhileRemembering()) {
                json.append(", \"").append(toggle.configKey()).append("\": true");
            }
        }
        json.append('}');
        Files.writeString(configFile, json.toString());

        assertTrue(ModConfig.load(configFile));
        assertFalse(ModConfig.get(Toggle.REMEMBER));
        for (Toggle toggle : Toggle.values()) {
            if (toggle.persistedWhileRemembering()) {
                assertFalse(ModConfig.get(toggle), toggle + " must start off");
            }
        }
    }

    @Test
    void loadWithRememberRestoresEveryFeature() {
        ModConfig.set(Toggle.REMEMBER, true);
        for (Toggle toggle : Toggle.values()) {
            if (toggle.persistedWhileRemembering()) {
                ModConfig.set(toggle, true);
            }
        }
        assertTrue(ModConfig.save(configFile));

        resetToDefaults();
        assertTrue(ModConfig.load(configFile));

        assertTrue(ModConfig.get(Toggle.REMEMBER));
        for (Toggle toggle : Toggle.values()) {
            if (toggle.persistedWhileRemembering()) {
                assertTrue(ModConfig.get(toggle), toggle + " must be restored");
            }
        }
    }

    @Test
    void missingFileIsCreatedWithDefaults() throws Exception {
        assertFalse(Files.exists(configFile));

        assertTrue(ModConfig.load(configFile));

        assertTrue(Files.exists(configFile));
        for (Toggle toggle : Toggle.values()) {
            assertEquals(toggle.defaultValue(), ModConfig.get(toggle), toggle + " default");
        }
    }

    @Test
    void malformedJsonKeepsCurrentValues() throws Exception {
        Files.writeString(configFile, "{ this is not json");
        ModConfig.set(Toggle.SKIN, true);
        ModConfig.set(Toggle.REMEMBER, false);

        assertFalse(ModConfig.load(configFile));

        assertTrue(ModConfig.get(Toggle.SKIN));
        assertFalse(ModConfig.get(Toggle.REMEMBER));
    }

    @Test
    void partialFileKeepsUnspecifiedDefaults() throws Exception {
        Files.writeString(configFile, "{\"rememberState\": true, \"showInvisibleSkin\": true}");

        assertTrue(ModConfig.load(configFile));

        assertTrue(ModConfig.get(Toggle.REMEMBER));
        assertTrue(ModConfig.get(Toggle.SKIN));
        for (Toggle toggle : Toggle.values()) {
            if (toggle != Toggle.REMEMBER && toggle != Toggle.SKIN) {
                assertEquals(toggle.defaultValue(), ModConfig.get(toggle), toggle + " default");
            }
        }
    }

    @Test
    void unknownKeysAreIgnored() throws Exception {
        Files.writeString(configFile, "{\"rememberState\": true, \"showInvisibleNametag\": true, \"foo\": 123}");

        assertTrue(ModConfig.load(configFile));

        assertTrue(ModConfig.get(Toggle.NAMETAG));
        assertFalse(ModConfig.get(Toggle.SKIN));
    }

    @Test
    void wrongValueTypeIsIgnored() throws Exception {
        Files.writeString(configFile, "{\"rememberState\": \"yes\", \"showInvisibleSkin\": \"yes\"}");
        // Remember defaults to on, so turn it off first: a wrong-typed key must keep the
        // current value instead of snapping back to the declared default.
        ModConfig.set(Toggle.REMEMBER, false);
        ModConfig.set(Toggle.SKIN, true);

        assertTrue(ModConfig.load(configFile));

        assertFalse(ModConfig.get(Toggle.REMEMBER),
            "a wrong-typed rememberState must keep the current value");
        assertFalse(ModConfig.get(Toggle.SKIN),
            "a wrong-typed feature value must not be read as true");
    }

    @Test
    void rememberIsTheOnlyToggleOnByDefault() {
        for (Toggle toggle : Toggle.values()) {
            assertEquals(toggle == Toggle.REMEMBER, toggle.defaultValue(),
                toggle + ": only rememberState may default to on");
        }
    }

    private static void setAllFeatures(boolean value) {
        for (Toggle toggle : Toggle.values()) {
            if (toggle.persistedWhileRemembering()) {
                ModConfig.set(toggle, value);
            }
        }
    }
}
