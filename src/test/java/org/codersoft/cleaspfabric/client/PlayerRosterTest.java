package org.codersoft.cleaspfabric.client;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerRosterTest {

    private static final UUID VIEWER = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    @Test
    void anotherPlayerIsASubject() {
        assertTrue(PlayerRoster.isSubject(VIEWER, OTHER, false));
    }

    @Test
    void theViewerIsNeverASubject() {
        assertFalse(PlayerRoster.isSubject(VIEWER, VIEWER, false));
    }

    @Test
    void spectatorsAreNeverSubjects() {
        assertFalse(PlayerRoster.isSubject(VIEWER, OTHER, true));
    }
}
