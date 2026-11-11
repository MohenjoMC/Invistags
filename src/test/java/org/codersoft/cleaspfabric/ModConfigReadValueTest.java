package org.codersoft.cleaspfabric;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The config reader's fallbacks: a missing or wrong-typed key must not silently turn a
 * default-on setting off, and remember-off must ignore stored feature values.
 */
class ModConfigReadValueTest {

    @Test
    void missingKeyFallsBackToTheDeclaredDefault() {
        JsonObject empty = new JsonObject();

        assertTrue(ModConfig.readValue(empty, Toggle.REMEMBER, true),
            "rememberState defaults to on");
        assertFalse(ModConfig.readValue(empty, Toggle.SKIN, true));
    }

    @Test
    void storedValueWinsOverTheDefault() {
        JsonObject json = new JsonObject();
        json.addProperty(Toggle.SKIN.configKey(), true);

        assertTrue(ModConfig.readValue(json, Toggle.SKIN, true));
    }

    @Test
    void wrongTypeFallsBackToTheDeclaredDefault() {
        JsonObject json = new JsonObject();
        json.addProperty(Toggle.REMEMBER.configKey(), "yes");

        assertTrue(ModConfig.readValue(json, Toggle.REMEMBER, true));
    }

    @Test
    void rememberOffIgnoresAStoredFeatureValue() {
        JsonObject json = new JsonObject();
        json.addProperty(Toggle.SKIN.configKey(), true);

        assertFalse(ModConfig.readValue(json, Toggle.SKIN, false));
    }
}
