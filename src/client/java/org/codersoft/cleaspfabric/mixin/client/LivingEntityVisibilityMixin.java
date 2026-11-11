package org.codersoft.cleaspfabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.Toggle;
import org.codersoft.cleaspfabric.client.InvisibleRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws the model of an invisible entity at full opacity.
 *
 * <p>The flags are shared with the label and layer passes, so the reveal clears them for
 * the duration of {@code submit} and restores them at the end. Layers that check them,
 * such as capes and mob details, therefore render too, and the label rule reads the
 * extracted value from {@link InvisibleRenderState}.
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityVisibilityMixin {

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("RETURN")
    )
    private void invistags$rememberInvisibility(
        LivingEntity entity,
        LivingEntityRenderState state,
        float partialTicks,
        CallbackInfo ci
    ) {
        ((InvisibleRenderState) state).invistags$setWasInvisible(
            state.isInvisible, state.isInvisibleToPlayer);
    }

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("HEAD")
    )
    private void invistags$revealModel(
        LivingEntityRenderState state,
        PoseStack poseStack,
        SubmitNodeCollector collector,
        CameraRenderState cameraState,
        CallbackInfo ci
    ) {
        if (!state.isInvisible) {
            return;
        }
        boolean reveal = state instanceof AvatarRenderState
            ? ModConfig.get(Toggle.SKIN)
            : ModConfig.get(Toggle.MOBS);
        if (reveal) {
            state.isInvisible = false;
            state.isInvisibleToPlayer = false;
        }
    }

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("RETURN")
    )
    private void invistags$restoreInvisibility(
        LivingEntityRenderState state,
        PoseStack poseStack,
        SubmitNodeCollector collector,
        CameraRenderState cameraState,
        CallbackInfo ci
    ) {
        InvisibleRenderState flags = (InvisibleRenderState) state;
        state.isInvisible = flags.invistags$wasInvisible();
        state.isInvisibleToPlayer = flags.invistags$wasInvisibleToPlayer();
    }
}
