package org.codersoft.cleaspfabric.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The one place that decides which players the reveal and alert rules may consider:
 * everyone in the client level except the local player and spectators.
 *
 * <p>Both the gear cache and the toast notifier read this, so the rule cannot drift
 * between them.
 */
public final class PlayerRoster {

    private PlayerRoster() {
    }

    /**
     * @return the players to consider this tick, empty when there is no level or player
     */
    public static List<Player> subjects(Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null) {
            return List.of();
        }
        List<Player> subjects = new ArrayList<>();
        for (Player player : level.players()) {
            if (isSubject(client.player.getUUID(), player.getUUID(), player.isSpectator())) {
                subjects.add(player);
            }
        }
        return subjects;
    }

    /**
     * The rule itself, kept free of Minecraft types so it can be unit tested.
     */
    public static boolean isSubject(UUID viewerId, UUID candidateId, boolean spectator) {
        return !candidateId.equals(viewerId) && !spectator;
    }
}
