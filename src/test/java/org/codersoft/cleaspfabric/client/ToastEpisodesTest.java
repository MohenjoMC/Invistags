package org.codersoft.cleaspfabric.client;

import org.codersoft.cleaspfabric.ToastEpisodes;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToastEpisodesTest {

    private static final UUID HIDDEN = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final double RADIUS_SQUARED = 64.0 * 64.0;

    private final ToastEpisodes episodes = new ToastEpisodes();

    @Test
    void noInvisiblePlayersMeansNoToasts() {
        assertTrue(episodes.update(RADIUS_SQUARED, List.of(visible(HIDDEN, 1.0))).isEmpty());
    }

    @Test
    void newNearbyInvisiblePlayerIsSelected() {
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));
    }

    @Test
    void invisibleOutsideRadiusIsNotSelectedUntilApproach() {
        assertTrue(episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, RADIUS_SQUARED + 1.0))).isEmpty());

        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 4.0))));
    }

    @Test
    void announcedPlayerIsToastedOnlyOnce() {
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));

        assertTrue(episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))).isEmpty());
    }

    @Test
    void onlyUnannouncedPlayersAreSelected() {
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));

        assertEquals(Set.of(OTHER), episodes.update(
            RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0), invisible(OTHER, 1.0))));
    }

    @Test
    void walkingOutOfRangeWhileInvisibleDoesNotRepeat() {
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));

        assertTrue(episodes.update(RADIUS_SQUARED,
            List.of(invisible(HIDDEN, RADIUS_SQUARED + 1.0))).isEmpty());
        assertTrue(episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))).isEmpty());
    }

    @Test
    void oneAbsentTickDoesNotReArmThePlayer() {
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));

        assertTrue(episodes.update(RADIUS_SQUARED, List.of()).isEmpty());
        assertTrue(episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))).isEmpty());
    }

    @Test
    void absenceUpToTheGraceKeepsTheEpisode() {
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));

        advanceAbsent(ToastEpisodes.ABSENCE_GRACE_TICKS);
        assertTrue(episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))).isEmpty());
    }

    @Test
    void absencePastTheGraceReArmsThePlayer() {
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));

        advanceAbsent(ToastEpisodes.ABSENCE_GRACE_TICKS + 1);
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));
    }

    @Test
    void graceCountsConsecutiveAbsentTicksOnly() {
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));

        advanceAbsent(ToastEpisodes.ABSENCE_GRACE_TICKS);
        assertTrue(episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))).isEmpty());
        advanceAbsent(ToastEpisodes.ABSENCE_GRACE_TICKS);
        assertTrue(episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))).isEmpty());
    }

    @Test
    void becomingVisibleReArmsThePlayer() {
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));

        assertTrue(episodes.update(RADIUS_SQUARED, List.of(visible(HIDDEN, 1.0))).isEmpty());
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));
    }

    @Test
    void leavingTheWorldReArmsThePlayer() {
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));

        advanceAbsent(ToastEpisodes.ABSENCE_GRACE_TICKS + 1);
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));
    }

    @Test
    void radiusBoundaryIsInclusive() {
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, RADIUS_SQUARED))));
    }

    @Test
    void resetClearsEpisodeState() {
        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));
        assertTrue(episodes.update(RADIUS_SQUARED, List.of()).isEmpty());

        episodes.reset();

        assertEquals(Set.of(HIDDEN), episodes.update(RADIUS_SQUARED, List.of(invisible(HIDDEN, 1.0))));
    }

    @Test
    void nullSightingsReturnsEmpty() {
        assertTrue(episodes.update(RADIUS_SQUARED, null).isEmpty());
    }

    @Test
    void emptySightingsReturnsEmpty() {
        assertTrue(episodes.update(RADIUS_SQUARED, List.of()).isEmpty());
    }

    /** Runs {@code ticks} ticks in which no player is sighted at all. */
    private void advanceAbsent(int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            assertTrue(episodes.update(RADIUS_SQUARED, List.of()).isEmpty(), "tick " + tick);
        }
    }

    private static ToastEpisodes.Sighting invisible(UUID id, double distanceSquared) {
        return new ToastEpisodes.Sighting(id, true, distanceSquared);
    }

    private static ToastEpisodes.Sighting visible(UUID id, double distanceSquared) {
        return new ToastEpisodes.Sighting(id, false, distanceSquared);
    }
}
