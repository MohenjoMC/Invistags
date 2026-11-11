package org.codersoft.cleaspfabric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks invisibility episodes and decides which players still need a toast.
 * <p>
 * A player is announced once per invisibility episode. Episodes are keyed by
 * player id and stay open for as long as that id is reported invisible, so
 * walking out of and back into range does not raise a second toast. An id that
 * is missing from the sightings altogether — for example because the player
 * momentarily left entity-tracking range — keeps its episode for up to
 * {@link #ABSENCE_GRACE_TICKS} consecutive ticks and only then re-arms; the
 * grace is what separates "briefly untracked" from "left the world". An id
 * reported visible re-arms immediately. Players farther away than the supplied
 * radius are not announced until they come within range.
 * <p>
 * Kept free of Minecraft types so it can be unit tested without launching a
 * client.
 */
public final class ToastEpisodes {

    /**
     * Consecutive ticks an announced id may be missing from the sightings before
     * its episode ends and the player becomes eligible for a new toast.
     */
    public static final int ABSENCE_GRACE_TICKS = 100;

    private final Set<UUID> announced = new HashSet<>();
    private final Map<UUID, Integer> absentTicks = new HashMap<>();

    /**
     * A single player as seen by the notifier during one tick.
     *
     * @param id              the player's UUID
     * @param invisible       whether the player is currently invisible
     * @param distanceSquared squared distance from the viewer, in blocks squared
     */
    public record Sighting(UUID id, boolean invisible, double distanceSquared) {
    }

    /**
     * Applies one tick of sightings and returns the ids that just entered an
     * invisibility episode within the radius. Ids returned by an earlier call are
     * not returned again until their episode ends.
     *
     * @param radiusSquared squared alert radius, inclusive
     * @param sightings     this tick's revealable players, may be {@code null} or empty
     * @return the ids that need a toast now, empty when there are none
     */
    public Set<UUID> update(double radiusSquared, Collection<Sighting> sightings) {
        Set<UUID> stillInvisible = new HashSet<>();
        Set<UUID> fresh = new HashSet<>();

        if (sightings != null) {
            for (Sighting sighting : sightings) {
                if (sighting == null || sighting.id() == null) {
                    continue;
                }
                UUID id = sighting.id();
                if (!sighting.invisible()) {
                    // Seen and visible: the episode is over, no grace applies.
                    announced.remove(id);
                    absentTicks.remove(id);
                    continue;
                }
                stillInvisible.add(id);
                absentTicks.remove(id);
                if (sighting.distanceSquared() <= radiusSquared && announced.add(id)) {
                    fresh.add(id);
                }
            }
        }

        // Ids missing from this tick start, or continue, their absence count.
        for (UUID id : new ArrayList<>(announced)) {
            if (stillInvisible.contains(id)) {
                continue;
            }
            int absent = absentTicks.merge(id, 1, (count, one) -> Math.min(count + one, ABSENCE_GRACE_TICKS + 1));
            if (absent > ABSENCE_GRACE_TICKS) {
                announced.remove(id);
                absentTicks.remove(id);
            }
        }
        return fresh;
    }

    /**
     * Clears every episode, so all players become eligible for a toast again.
     */
    public void reset() {
        announced.clear();
        absentTicks.clear();
    }
}
