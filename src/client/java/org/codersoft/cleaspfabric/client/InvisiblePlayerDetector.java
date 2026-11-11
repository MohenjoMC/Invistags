package org.codersoft.cleaspfabric.client;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Pure-Java selection of invisible players that still need a toast. Kept free of
 * Minecraft types so it can be unit tested without launching a client.
 */
public final class InvisiblePlayerDetector {

    private InvisiblePlayerDetector() {
    }

    /**
     * Returns the UUIDs that are nearby and invisible but have not been announced yet.
     */
    public static Set<UUID> findNewlyInvisible(Collection<UUID> nearbyInvisible,
                                               Set<UUID> alreadyNotified) {
        if (nearbyInvisible == null) {
            return new HashSet<>();
        }
        Set<UUID> fresh = new HashSet<>(nearbyInvisible);
        fresh.removeAll(alreadyNotified);
        return fresh;
    }
}
