package org.codersoft.cleaspfabric.client;

import org.codersoft.cleaspfabric.ToastEpisodes;

import org.codersoft.cleaspfabric.ModConfig;

import org.codersoft.cleaspfabric.Toggle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;

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
    private static ClientWorld trackedWorld;

    private InvisiblePlayerNotifier() {
    }

    public static void tick(MinecraftClient client) {
        if (!ModConfig.get(Toggle.TOAST)) {
            // Keep the episodes: re-enabling must not re-announce players who never
            // became visible in the meantime.
            return;
        }

        ClientWorld world = client.world;
        if (world == null || client.player == null) {
            clear();
            return;
        }
        if (world != trackedWorld) {
            EPISODES.reset();
            trackedWorld = world;
        }

        List<PlayerEntity> players = PlayerRoster.subjects(client);
        List<ToastEpisodes.Sighting> sightings = new ArrayList<>(players.size());
        Map<UUID, PlayerEntity> byId = new HashMap<>(players.size());
        for (PlayerEntity player : players) {
            byId.put(player.getUuid(), player);
            sightings.add(new ToastEpisodes.Sighting(
                player.getUuid(),
                player.isInvisible(),
                client.player.squaredDistanceTo(player)));
        }

        Set<UUID> fresh = EPISODES.update(ALERT_RADIUS * ALERT_RADIUS, sightings);
        for (UUID id : fresh) {
            PlayerEntity player = byId.get(id);
            if (player != null) {
                showToast(client, player);
            }
        }
    }

    /**
     * Raises a toast for a player immediately, used by the client gametest.
     */
    public static void showPreview(MinecraftClient client, PlayerEntity player) {
        showToast(client, player);
    }

    private static void showToast(MinecraftClient client, PlayerEntity player) {
        SkinTextures skin = skinFor(client, player);
        client.getToastManager().add(new InvisiblePlayerToast(
            skin,
            player.getName(),
            Text.translatable("message.invistags.toast.invisible")));
    }

    private static SkinTextures skinFor(MinecraftClient client, PlayerEntity player) {
        if (client.getNetworkHandler() != null) {
            PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(player.getUuid());
            if (entry != null) {
                return entry.getSkinTextures();
            }
        }
        return DefaultSkinHelper.getSkinTextures(player.getUuid());
    }

    public static void clear() {
        EPISODES.reset();
        trackedWorld = null;
    }
}
