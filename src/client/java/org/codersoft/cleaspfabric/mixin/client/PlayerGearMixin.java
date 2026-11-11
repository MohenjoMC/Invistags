package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.codersoft.cleaspfabric.client.GearReveal;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Gives the render-state extraction a temporary view of remembered gear, and marks
 * flagged players' labels.
 */
@Mixin(PlayerEntityRenderer.class)
public class PlayerGearMixin {

    @Unique
    private Map<EquipmentSlot, ItemStack> invistags$replaced = Map.of();

    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("HEAD")
    )
    private void invistags$equipRememberedGear(
        PlayerLikeEntity entity,
        PlayerEntityRenderState state,
        float tickDelta,
        CallbackInfo ci
    ) {
        invistags$replaced = Map.of();
        if (!ModConfig.showInvisibleGear) return;
        if (!(entity instanceof PlayerEntity player) || !player.isInvisible()) return;
        invistags$replaced = GearReveal.equipMissing(player);
    }

    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("RETURN")
    )
    private void invistags$restoreGear(
        PlayerLikeEntity entity,
        PlayerEntityRenderState state,
        float tickDelta,
        CallbackInfo ci
    ) {
        if (entity instanceof PlayerEntity player && !invistags$replaced.isEmpty()) {
            GearReveal.restore(player, invistags$replaced);
        }
        invistags$replaced = Map.of();
    }
}
