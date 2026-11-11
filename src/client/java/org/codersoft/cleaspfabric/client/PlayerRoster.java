package org.codersoft.cleaspfabric.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The one place that decides which players the reveal and alert rules may consider:
 * everyone in the client world except the local player and spectators.
 *
 * <p>Both the gear cache and the toast notifier read this, so the rule cannot drift
 * between them.
 */
public final class PlayerRoster {

    private PlayerRoster() {
    }

    /**
     * @return the players to consider this tick, empty when there is no world or player
     */
    public static List<PlayerEntity> subjects(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || client.player == null) {
            return List.of();
        }
        List<PlayerEntity> subjects = new ArrayList<>();
        for (PlayerEntity player : world.getPlayers()) {
            if (isSubject(client.player.getUuid(), player.getUuid(), player.isSpectator())) {
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
