package org.codersoft.cleaspfabric.client;

import org.codersoft.cleaspfabric.ModConfig;

import org.codersoft.cleaspfabric.Toggle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Remembers the last equipment a player was seen wearing and fills the gaps in that
 * player's render state while they are invisible.
 *
 * <p>Vanilla sends equipment even for invisible players. Servers that want invisible
 * players to look empty send the same packet with empty stacks instead. A client can
 * only show gear it has seen, so the cache is filled while the player is visible and is
 * never cleared by an empty stack received while the player is invisible.
 *
 * <p>The render path only reads the cache: {@link #decorate} writes the cached stacks
 * into the render state, so the live player entity is never modified and there is
 * nothing to restore if rendering fails.
 */
public final class GearReveal {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };

    private static final EquipmentSlot[] HAND_SLOTS = {
        EquipmentSlot.MAINHAND,
        EquipmentSlot.OFFHAND
    };

    private static final EquipmentSlot[] TRACKED_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET,
        EquipmentSlot.MAINHAND,
        EquipmentSlot.OFFHAND
    };

    private static final Map<UUID, EnumMap<EquipmentSlot, ItemStack>> REMEMBERED = new HashMap<>();
    private static ClientWorld trackedWorld;

    private GearReveal() {
    }

    /**
     * Maintains the cache once per client tick: tracks the world, honours the toggle and
     * snapshots visible players.
     */
    public static void tick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null) {
            clear();
            return;
        }
        if (world != trackedWorld) {
            REMEMBERED.clear();
            trackedWorld = world;
        }
        if (!ModConfig.get(Toggle.GEAR)) {
            REMEMBERED.clear();
            return;
        }

        for (PlayerEntity player : PlayerRoster.subjects(client)) {
            if (player.isInvisible()) {
                mergeNonEmpty(player);
            } else {
                REMEMBERED.put(player.getUuid(), snapshot(player));
            }
        }
    }

    /**
     * Writes remembered gear into slots the server left empty, after the vanilla
     * extraction filled the render state but before it is drawn.
     *
     * <p>Nothing is written back to the entity or to the cache, so this is safe to call
     * for every frame and for any player.
     */
    public static void decorate(
        PlayerEntity player,
        BipedEntityRenderState state,
        ItemModelManager itemModelResolver
    ) {
        EnumMap<EquipmentSlot, ItemStack> remembered = REMEMBERED.get(player.getUuid());
        if (remembered == null || remembered.isEmpty()) {
            return;
        }

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!player.getEquippedStack(slot).isEmpty()) {
                continue;
            }
            ItemStack cached = remembered.get(slot);
            if (cached == null || cached.isEmpty()) {
                continue;
            }
            ItemStack copy = cached.copy();
            switch (slot) {
                case HEAD -> state.equippedHeadStack = copy;
                case CHEST -> state.equippedChestStack = copy;
                case LEGS -> state.equippedLegsStack = copy;
                case FEET -> state.equippedFeetStack = copy;
                default -> {
                }
            }
        }

        for (EquipmentSlot slot : HAND_SLOTS) {
            ItemStack cached = remembered.get(slot);
            if (cached == null || cached.isEmpty()) {
                continue;
            }
            Arm arm = slot == EquipmentSlot.MAINHAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
            if (!player.getStackInArm(arm).isEmpty()) {
                continue;
            }
            ItemDisplayContext context = arm == Arm.RIGHT
                ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            if (arm == Arm.RIGHT) {
                itemModelResolver.updateForLivingEntity(
                    state.rightHandItemState, cached, context, player);
                state.rightHandItem = cached.copy();
            } else {
                itemModelResolver.updateForLivingEntity(
                    state.leftHandItemState, cached, context, player);
                state.leftHandItem = cached.copy();
            }
        }
    }

    public static void clear() {
        REMEMBERED.clear();
        trackedWorld = null;
    }

    private static EnumMap<EquipmentSlot, ItemStack> snapshot(PlayerEntity player) {
        EnumMap<EquipmentSlot, ItemStack> snapshot = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : TRACKED_SLOTS) {
            snapshot.put(slot, player.getEquippedStack(slot).copy());
        }
        return snapshot;
    }

    private static void mergeNonEmpty(PlayerEntity player) {
        EnumMap<EquipmentSlot, ItemStack> remembered = REMEMBERED.computeIfAbsent(
            player.getUuid(), id -> new EnumMap<>(EquipmentSlot.class));
        for (EquipmentSlot slot : TRACKED_SLOTS) {
            ItemStack live = player.getEquippedStack(slot);
            if (!live.isEmpty()) {
                remembered.put(slot, live.copy());
            }
        }
    }
}
