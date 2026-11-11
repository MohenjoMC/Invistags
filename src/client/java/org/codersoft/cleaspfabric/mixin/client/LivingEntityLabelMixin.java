package org.codersoft.cleaspfabric.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.NametagPolicy;
import org.codersoft.cleaspfabric.Toggle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets an invisible entity keep its label by answering only the invisibility half of
 * {@code hasLabel} for it.
 *
 * <p>Vanilla keeps deciding everything else: the crouching 32-block cutoff, the F1 HUD
 * check, the camera entity, passengers, the 64-block name distance and the scoreboard
 * team visibility rules all run unchanged, and the player renderer still applies its own
 * "should this player show a name" check on top of the super call.
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityLabelMixin<T extends LivingEntity> {

    /**
     * Reports the entity as not invisible to the local player when the reveal policy
     * wants its label, so vanilla's {@code bl} term turns true and the rest of
     * {@code hasLabel} is left intact.
     */
    @ModifyExpressionValue(
        method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/entity/player/PlayerEntity;)Z"
        )
    )
    private boolean invistags$forceLabelVisible(
        boolean original,
        @Local(argsOnly = true) LivingEntity entity
    ) {
        if (!original) {
            return original;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return original;
        }

        NametagPolicy.Subject subject;
        if (entity instanceof PlayerEntity player) {
            subject = NametagPolicy.Subject.player(player.isInvisible(), player == client.player);
        } else {
            subject = NametagPolicy.Subject.mob(
                entity.isInvisible(), entity.isCustomNameVisible());
        }

        boolean reveal = NametagPolicy.decide(
            subject,
            ModConfig.get(Toggle.NAMETAG),
            ModConfig.get(Toggle.MOBS)
        ).showLabel();
        return !reveal;
    }
}
