package org.codersoft.cleaspfabric.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.NametagPolicy;
import org.codersoft.cleaspfabric.Toggle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Lets an invisible entity keep its label by answering only the invisibility half of
 * {@code shouldShowName} for it.
 *
 * <p>Vanilla keeps deciding everything else: the crouching 32-block cutoff, the F1 HUD
 * check, the camera entity, passengers, the 64-block name distance and the scoreboard
 * team visibility rules all run unchanged.
 */
@Mixin(LivingEntityRenderer.class)
public class LivingEntityLabelMixin {

    @ModifyExpressionValue(
        method = "shouldShowName(Lnet/minecraft/world/entity/LivingEntity;D)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;isInvisibleTo(Lnet/minecraft/world/entity/player/Player;)Z"
        )
    )
    private boolean invistags$forceLabelVisible(
        boolean original,
        @Local(argsOnly = true) LivingEntity entity
    ) {
        if (!original) {
            return original;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return original;
        }

        NametagPolicy.Subject subject;
        if (entity instanceof Player player) {
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
