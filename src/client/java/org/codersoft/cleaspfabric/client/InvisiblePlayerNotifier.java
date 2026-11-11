package org.codersoft.cleaspfabric.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.Toggle;
import org.codersoft.cleaspfabric.ToastEpisodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Shows a toast with the player's name and head the first time an invisible player
 * comes within {@link #ALERT_RADIUS} blocks. Episode tracking lives in
 * {@link ToastEpisodes} and the player filter lives in {@link PlayerRoster}; this class
 * only adapts the Minecraft client to them.
 */
public final class InvisiblePlayerNotifier {

    /** Players farther away than this are not announced yet. */
    public static final double ALERT_RADIUS = 64.0;

    private static final ToastEpisodes EPISODES = new ToastEpisodes();
    private static ClientLevel trackedLevel;

    private InvisiblePlayerNotifier() {
    }

    public static void tick(Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null) {
            clear();
            return;
        }
        if (!ModConfig.get(Toggle.TOAST)) {
            // Keep the episodes: disabling and re-enabling must not re-announce.
            return;
        }
        if (level != trackedLevel) {
            EPISODES.reset();
            trackedLevel = level;
        }

        List<Player> players = PlayerRoster.subjects(client);
        List<ToastEpisodes.Sighting> sightings = new ArrayList<>(players.size());
        Map<UUID, Player> byId = new HashMap<>(players.size());
        for (Player player : players) {
            byId.put(player.getUUID(), player);
            sightings.add(new ToastEpisodes.Sighting(
                player.getUUID(),
                player.isInvisible(),
                client.player.distanceToSqr(player)));
        }

        Set<UUID> fresh = EPISODES.update(ALERT_RADIUS * ALERT_RADIUS, sightings);
        for (UUID id : fresh) {
            Player player = byId.get(id);
            if (player != null) {
                showToast(client, player);
            }
        }
    }

    /**
     * Raises a toast for a player immediately, used by the client gametest.
     */
    public static void showPreview(Minecraft client, Player player) {
        showToast(client, player);
    }

    private static void showToast(Minecraft client, Player player) {
        PlayerSkin skin = skinFor(client, player);
        client.gui.toastManager().addToast(new InvisiblePlayerToast(
            skin,
            player.getName(),
            Component.translatable("message.invistags.toast.invisible")));
    }

    private static PlayerSkin skinFor(Minecraft client, Player player) {
        if (client.getConnection() != null) {
            PlayerInfo entry = client.getConnection().getPlayerInfo(player.getUUID());
            if (entry != null) {
                return entry.getSkin();
            }
        }
        return DefaultPlayerSkin.get(player.getUUID());
    }

    public static void clear() {
        EPISODES.reset();
        trackedLevel = null;
    }
}
