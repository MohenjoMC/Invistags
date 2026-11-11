package org.codersoft.cleaspfabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.NametagPolicy;
import org.codersoft.cleaspfabric.Toggle;
import org.codersoft.cleaspfabric.client.InvisibleRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Keeps a forced invisible-player label visible through blocks while the player is
 * sneaking. Vanilla drops the through-block copy for a crouching entity, so the flag is
 * forced back on. The extracted invisibility comes from {@link InvisibleRenderState}
 * because the model reveal clears the render-state flags.
 */
@Mixin(EntityRenderer.class)
public class LabelSneakMixin {

    @ModifyArg(
        method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
        ),
        index = 4
    )
    private boolean invistags$keepLabelThroughBlocks(
        boolean seeThrough,
        @Local(argsOnly = true) EntityRenderState state
    ) {
        if (seeThrough || !ModConfig.get(Toggle.NAMETAG)) {
            return seeThrough;
        }
        NametagPolicy.Decision decision = NametagPolicy.decide(
            NametagPolicy.Subject.player(
                ((InvisibleRenderState) state).invistags$wasInvisible(), false),
            true,
            ModConfig.get(Toggle.MOBS));
        return decision.seeThrough();
    }
}
