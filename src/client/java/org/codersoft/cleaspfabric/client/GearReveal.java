package org.codersoft.cleaspfabric.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.Toggle;

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
    private static ClientLevel trackedLevel;

    private GearReveal() {
    }

    /**
     * Maintains the cache once per client tick: tracks the level, honours the toggle and
     * snapshots visible players.
     */
    public static void tick(Minecraft client) {
        ClientLevel level = client.level;
        if (level == null) {
            clear();
            return;
        }
        if (level != trackedLevel) {
            REMEMBERED.clear();
            trackedLevel = level;
        }
        if (!ModConfig.get(Toggle.GEAR)) {
            // Keep the cache: disabling and re-enabling must not lose seen gear.
            return;
        }

        for (Player player : PlayerRoster.subjects(client)) {
            if (player.isInvisible()) {
                mergeNonEmpty(player);
            } else {
                REMEMBERED.put(player.getUUID(), snapshot(player));
            }
        }
    }

    /**
     * Writes remembered gear into slots the server left empty, after the vanilla
     * extraction filled the render state but before it is drawn.
     */
    public static void decorate(
        Player player,
        HumanoidRenderState state,
        ItemModelResolver itemModelResolver
    ) {
        EnumMap<EquipmentSlot, ItemStack> remembered = REMEMBERED.get(player.getUUID());
        if (remembered == null || remembered.isEmpty()) {
            return;
        }

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (!player.getItemBySlot(slot).isEmpty()) {
                continue;
            }
            ItemStack cached = remembered.get(slot);
            if (cached == null || cached.isEmpty()) {
                continue;
            }
            ItemStack copy = cached.copy();
            switch (slot) {
                case HEAD -> state.headEquipment = copy;
                case CHEST -> state.chestEquipment = copy;
                case LEGS -> state.legsEquipment = copy;
                case FEET -> state.feetEquipment = copy;
                default -> {
                }
            }
        }

        for (EquipmentSlot slot : HAND_SLOTS) {
            ItemStack cached = remembered.get(slot);
            if (cached == null || cached.isEmpty()) {
                continue;
            }
            HumanoidArm arm = slot == EquipmentSlot.MAINHAND
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
            if (!player.getItemHeldByArm(arm).isEmpty()) {
                continue;
            }
            ItemDisplayContext context = arm == HumanoidArm.RIGHT
                ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            if (arm == HumanoidArm.RIGHT) {
                itemModelResolver.updateForLiving(
                    state.rightHandItemState, cached, context, player);
                state.rightHandItemStack = cached.copy();
            } else {
                itemModelResolver.updateForLiving(
                    state.leftHandItemState, cached, context, player);
                state.leftHandItemStack = cached.copy();
            }
        }
    }

    /**
     * Lifts the arm that holds a restored item. The avatar renderer assigns the poses
     * from the live stacks after the equipment extraction, so this runs last.
     */
    public static void applyArmPoses(Player player, AvatarRenderState state) {
        if (!ModConfig.get(Toggle.GEAR) || !player.isInvisible()) {
            return;
        }
        HumanoidArm mainArm = state.mainArm;
        applyArmPose(player, state, EquipmentSlot.MAINHAND, mainArm);
        applyArmPose(player, state, EquipmentSlot.OFFHAND, mainArm.getOpposite());
    }

    private static void applyArmPose(
        Player player,
        AvatarRenderState state,
        EquipmentSlot slot,
        HumanoidArm arm
    ) {
        ItemStack cached = REMEMBERED.getOrDefault(player.getUUID(), new EnumMap<>(EquipmentSlot.class))
            .get(slot);
        if (cached == null || cached.isEmpty()) {
            return;
        }
        if (!player.getItemHeldByArm(arm).isEmpty()) {
            return;
        }
        if (arm == HumanoidArm.RIGHT) {
            state.rightArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.ITEM;
        } else {
            state.leftArmPose = net.minecraft.client.model.HumanoidModel.ArmPose.ITEM;
        }
    }

    public static void clear() {
        REMEMBERED.clear();
        trackedLevel = null;
    }

    private static EnumMap<EquipmentSlot, ItemStack> snapshot(Player player) {
        EnumMap<EquipmentSlot, ItemStack> snapshot = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : TRACKED_SLOTS) {
            snapshot.put(slot, player.getItemBySlot(slot).copy());
        }
        return snapshot;
    }

    private static void mergeNonEmpty(Player player) {
        EnumMap<EquipmentSlot, ItemStack> remembered = REMEMBERED.computeIfAbsent(
            player.getUUID(), id -> new EnumMap<>(EquipmentSlot.class));
        for (EquipmentSlot slot : TRACKED_SLOTS) {
            ItemStack live = player.getItemBySlot(slot);
            if (!live.isEmpty()) {
                remembered.put(slot, live.copy());
            }
        }
    }
}
