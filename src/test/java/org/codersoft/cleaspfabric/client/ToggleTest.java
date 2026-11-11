package org.codersoft.cleaspfabric.client;

import org.codersoft.cleaspfabric.Toggle;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the catalogue invariants: every toggle must have a unique command and config
 * name, a translatable title and tooltip, and text for the status dump.
 */
class ToggleTest {

    @Test
    void noToggleShadowsTheConfigSubcommand() {
        for (Toggle toggle : Toggle.values()) {
            assertFalse("config".equals(toggle.commandName()),
                "a toggle named config would hide /invis config");
        }
    }

    @Test
    void commandAndConfigNamesAreUnique() {
        Set<String> commands = new HashSet<>();
        Set<String> configKeys = new HashSet<>();
        for (Toggle toggle : Toggle.values()) {
            assertTrue(commands.add(toggle.commandName()), "duplicate command: " + toggle.commandName());
            assertTrue(configKeys.add(toggle.configKey()), "duplicate config key: " + toggle.configKey());
        }
    }

    @Test
    void namesAreNonBlankAndUrlSafe() {
        for (Toggle toggle : Toggle.values()) {
            assertFalse(toggle.commandName().isBlank());
            assertTrue(toggle.commandName().matches("[a-z]+"), toggle + " command name");
            assertTrue(toggle.configKey().matches("[A-Za-z]+"), toggle + " config key");
            assertFalse(toggle.statusLabel().isBlank(), toggle + " status label");
            assertFalse(toggle.feedbackLabel().isBlank(), toggle + " feedback label");
        }
    }

    @Test
    void rememberIsTheOnlyUngatedToggle() {
        for (Toggle toggle : Toggle.values()) {
            assertEquals(toggle != Toggle.REMEMBER, toggle.persistedWhileRemembering(), toggle.toString());
        }
    }

    @Test
    void everyToggleHasATranslatedTitleAndTooltip() {
        JsonObject lang = readLang();
        for (Toggle toggle : Toggle.values()) {
            assertTrue(lang.has(toggle.labelKey()), "missing title for " + toggle + ": " + toggle.labelKey());
            assertTrue(lang.has(toggle.tooltipKey()), "missing tooltip for " + toggle + ": " + toggle.tooltipKey());
        }
    }

    private static JsonObject readLang() {
        try (InputStream stream = ToggleTest.class.getResourceAsStream(
            "/assets/invistags/lang/en_us.json")) {
            assertNotNull(stream, "en_us.json not on the test classpath");
            return JsonParser.parseReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            throw new AssertionError("could not read en_us.json", e);
        }
    }
}
