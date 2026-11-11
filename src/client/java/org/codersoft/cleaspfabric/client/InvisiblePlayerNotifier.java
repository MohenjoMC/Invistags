package org.codersoft.cleaspfabric.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Shows a toast with the player's name and head the first time an invisible player
 * comes within {@link #ALERT_RADIUS} blocks. A player is only announced once per
 * invisibility episode: becoming visible (or leaving the world) forgets them so a
 * later invisibility triggers a fresh toast, while walking in and out of range does
 * not repeat it.
 */
public final class InvisiblePlayerNotifier {

    /** Players farther away than this are not announced yet. */
    public static final double ALERT_RADIUS = 64.0;

    private static final Set<UUID> NOTIFIED = new HashSet<>();
    private static ClientWorld trackedWorld;

    private InvisiblePlayerNotifier() {
    }

    public static void tick(MinecraftClient client) {
        if (!ModConfig.toastInvisiblePlayers) {
            clear();
            return;
        }

        ClientWorld world = client.world;
        if (world == null || client.player == null) {
            clear();
            return;
        }
        if (world != trackedWorld) {
            NOTIFIED.clear();
            trackedWorld = world;
        }

        Set<UUID> invisibleIds = new HashSet<>();
        Set<UUID> nearbyInvisible = new HashSet<>();
        double radiusSquared = ALERT_RADIUS * ALERT_RADIUS;
        for (PlayerEntity player : world.getPlayers()) {
            if (player == client.player || player.isSpectator() || !player.isInvisible()) {
                continue;
            }
            invisibleIds.add(player.getUuid());
            if (client.player.squaredDistanceTo(player) <= radiusSquared) {
                nearbyInvisible.add(player.getUuid());
            }
        }

        // Forget players once they are visible again or no longer in the world.
        NOTIFIED.retainAll(invisibleIds);

        Set<UUID> fresh = InvisiblePlayerDetector.findNewlyInvisible(nearbyInvisible, NOTIFIED);
        for (PlayerEntity player : world.getPlayers()) {
            if (fresh.contains(player.getUuid())) {
                showToast(client, player);
            }
        }
        NOTIFIED.addAll(fresh);
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
        NOTIFIED.clear();
        trackedWorld = null;
    }
}
