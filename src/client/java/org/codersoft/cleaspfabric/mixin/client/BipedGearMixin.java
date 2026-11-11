package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.Toggle;
import org.codersoft.cleaspfabric.client.GearReveal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Gives an invisible player's render state the armour and held items the client last saw.
 *
 * <p>Runs after vanilla extracted the equipment into the render state and before any of it
 * is drawn. The live entity is never touched, so there is no swap to undo.
 *
 * <p>Only the biped extraction happens here; the player renderer assigns the arm poses
 * from the live stacks afterwards, which {@code PlayerArmPoseMixin} corrects.
 */
@Mixin(BipedEntityRenderer.class)
public class BipedGearMixin {

    @Inject(
        method = "updateBipedRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/BipedEntityRenderState;FLnet/minecraft/client/item/ItemModelManager;)V",
        at = @At("RETURN")
    )
    private static void invistags$fillRememberedGear(
        LivingEntity entity,
        BipedEntityRenderState state,
        float tickProgress,
        ItemModelManager itemModelResolver,
        CallbackInfo ci
    ) {
        if (!ModConfig.get(Toggle.GEAR)) {
            return;
        }
        if (entity instanceof PlayerEntity player && player.isInvisible()) {
            GearReveal.decorate(player, state, itemModelResolver);
        }
    }
}
