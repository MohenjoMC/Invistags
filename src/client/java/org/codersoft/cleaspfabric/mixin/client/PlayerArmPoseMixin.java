package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.codersoft.cleaspfabric.client.GearReveal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lifts the arms of a revealed invisible player so the cached hand items are held instead
 * of hanging at the side. The player renderer assigns the arm poses after the equipment
 * extraction filled the hand items, from the live stacks the server stripped.
 */
@Mixin(AvatarRenderer.class)
public class PlayerArmPoseMixin {

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("RETURN")
    )
    private void invistags$applyRevealedArmPoses(
        Avatar avatar,
        AvatarRenderState state,
        float partialTicks,
        CallbackInfo ci
    ) {
        if (avatar instanceof Player player) {
            GearReveal.applyArmPoses(player, state);
        }
    }
}
