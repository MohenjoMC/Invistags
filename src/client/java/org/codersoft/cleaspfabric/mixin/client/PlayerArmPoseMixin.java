package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.codersoft.cleaspfabric.client.GearReveal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lifts the arms of a revealed invisible player so the cached hand items are held instead
 * of hanging at the side.
 *
 * <p>The player renderer assigns the arm poses after the biped extraction filled the hand
 * items, and it derives them from the live stacks, which the server stripped. Running at
 * the end of {@code updateRenderState} lets the poses see the gear the state actually
 * carries.
 */
@Mixin(PlayerEntityRenderer.class)
public class PlayerArmPoseMixin {

    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V",
        at = @At("RETURN")
    )
    private void invistags$applyRevealedArmPoses(
        PlayerLikeEntity playerLikeEntity,
        PlayerEntityRenderState state,
        float tickProgress,
        CallbackInfo ci
    ) {
        if (playerLikeEntity instanceof PlayerEntity player) {
            GearReveal.applyArmPoses(player, state);
        }
    }
}
