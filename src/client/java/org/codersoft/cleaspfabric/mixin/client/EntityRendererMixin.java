package org.codersoft.cleaspfabric.mixin.client;

import org.codersoft.cleaspfabric.Toggle;

import org.codersoft.cleaspfabric.NametagPolicy;

import org.codersoft.cleaspfabric.ModConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
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
    private void forceShowInvisLabel(
        LivingEntity entity,
        double squaredDistanceToCamera,
        CallbackInfoReturnable<Boolean> cir
    ) {
        boolean players = ModConfig.get(Toggle.NAMETAG);
        boolean mobs = ModConfig.get(Toggle.MOBS);
        if (!players && !mobs) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        NametagPolicy.Subject subject;
        if (entity instanceof PlayerEntity player) {
            subject = NametagPolicy.Subject.player(
                player.isInvisible(), player == client.player);
        } else {
            subject = NametagPolicy.Subject.mob(
                entity.isInvisible(), entity.hasCustomName());
        }

        if (NametagPolicy.decide(subject, players, mobs).showLabel()) {
            cir.setReturnValue(true);
        }
    }
}
