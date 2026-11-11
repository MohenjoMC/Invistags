package org.codersoft.cleaspfabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.client.InvisibleRenderState;
import org.codersoft.cleaspfabric.NametagPolicy;
import org.codersoft.cleaspfabric.Toggle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Keeps a forced invisible-player label visible through blocks while the player is
 * sneaking. Vanilla drops the through-block copy for a crouching entity, but not for one
 * standing still, so the flag is forced back on instead of mutating the shared render
 * state.
 *
 * <p>{@code state.invisible} is cleared for the duration of the model reveal, so the
 * through-block decision reads the extracted value kept by {@link InvisibleRenderState}.
 */
@Mixin(PlayerEntityRenderer.class)
public class LabelSneakMixin {

    @ModifyArg(
        method = "renderLabelIfPresent",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitLabel(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Vec3d;ILnet/minecraft/text/Text;ZIDLnet/minecraft/client/render/state/CameraRenderState;)V"
        ),
        index = 4
    )
    private boolean invistags$keepLabelThroughBlocks(
        boolean notSneaking,
        @Local(argsOnly = true) PlayerEntityRenderState state
    ) {
        if (notSneaking || !ModConfig.get(Toggle.NAMETAG)) {
            return notSneaking;
        }
        NametagPolicy.Decision decision = NametagPolicy.decide(
            NametagPolicy.Subject.player(
                ((InvisibleRenderState) state).invistags$wasInvisible(), false),
            true,
            ModConfig.get(Toggle.MOBS));
        return decision.seeThrough();
    }
}
