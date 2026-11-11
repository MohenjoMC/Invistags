package org.codersoft.cleaspfabric.client;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvisiblePlayerDetectorTest {

    private static final UUID HIDDEN = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void noInvisiblePlayersMeansNoToasts() {
        assertTrue(InvisiblePlayerDetector.findNewlyInvisible(List.of(), Set.of()).isEmpty());
    }

    @Test
    void newInvisiblePlayerIsSelected() {
        Set<UUID> fresh = InvisiblePlayerDetector.findNewlyInvisible(List.of(HIDDEN), Set.of());

        assertEquals(Set.of(HIDDEN), fresh);
    }

    @Test
    void alreadyAnnouncedPlayerIsSkipped() {
        Set<UUID> fresh = InvisiblePlayerDetector.findNewlyInvisible(
            List.of(HIDDEN), Set.of(HIDDEN));

        assertTrue(fresh.isEmpty());
    }

    @Test
    void onlyUnannouncedPlayersAreSelected() {
        Set<UUID> fresh = InvisiblePlayerDetector.findNewlyInvisible(
            List.of(HIDDEN, OTHER), Set.of(HIDDEN));

        assertEquals(Set.of(OTHER), fresh);
    }

    @Test
    void nullInvisibleCollectionIsEmpty() {
        assertTrue(InvisiblePlayerDetector.findNewlyInvisible(null, Set.of()).isEmpty());
    }
}
