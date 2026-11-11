package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.codersoft.cleaspfabric.client.ModConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class EntityRendererMixin<T extends LivingEntity> {

    @Inject(
        method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z",
        at = @At("RETURN"),
        cancellable = true
    )
    private void forceShowInvisPlayerLabel(
        LivingEntity entity,
        double squaredDistanceToCamera,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!ModConfig.showInvisibleNametag && !ModConfig.showInvisibleMobs) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (entity instanceof PlayerEntity player) {
            if (!ModConfig.showInvisibleNametag) return;
            // Never show your own nametag through this.
            if (player == client.player) return;
            if (player.isInvisible()) {
                cir.setReturnValue(true);
            }
            return;
        }

        // Only custom-named mobs have a meaningful label; forcing vanilla mobs
        // would spam names like "Zombie" for every invisible entity.
        if (ModConfig.showInvisibleMobs && entity.isInvisible() && entity.hasCustomName()) {
            cir.setReturnValue(true);
        }
    }
}
