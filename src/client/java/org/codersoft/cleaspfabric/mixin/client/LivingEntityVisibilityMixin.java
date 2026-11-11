package org.codersoft.cleaspfabric.mixin.client;

import org.codersoft.cleaspfabric.client.InvisibleRenderState;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.Toggle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the model of an invisible entity at full opacity.
 *
 * <p>{@code state.invisible} is shared with the label and feature passes, so the reveal
 * clears it for the duration of {@code render} and then puts it back. Capes, ears, wool
 * and the other features that check the flag therefore render too, and nothing outside the
 * frame sees a changed state. {@link InvisibleRenderState} keeps the real value available
 * to the label rule.
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityVisibilityMixin {

    @Inject(
        method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
        at = @At("HEAD")
    )
    private void invistags$rememberInvisibility(
        LivingEntity entity,
        LivingEntityRenderState state,
        float tickProgress,
        CallbackInfo ci
    ) {
        ((InvisibleRenderState) state).invistags$setWasInvisible(entity.isInvisible());
    }

    @Inject(
        method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
        at = @At("HEAD")
    )
    private void invistags$revealModel(
        LivingEntityRenderState state,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        CameraRenderState cameraState,
        CallbackInfo ci
    ) {
        if (!state.invisible) {
            return;
        }
        boolean reveal = state instanceof PlayerEntityRenderState
            ? ModConfig.get(Toggle.SKIN)
            : ModConfig.get(Toggle.MOBS);
        if (reveal) {
            state.invisible = false;
        }
    }

    @Inject(
        method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
        at = @At("RETURN")
    )
    private void invistags$restoreInvisibility(
        LivingEntityRenderState state,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        CameraRenderState cameraState,
        CallbackInfo ci
    ) {
        state.invisible = ((InvisibleRenderState) state).invistags$wasInvisible();
    }
}
