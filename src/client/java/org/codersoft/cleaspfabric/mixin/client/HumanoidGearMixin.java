package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
 */
@Mixin(HumanoidMobRenderer.class)
public class HumanoidGearMixin {

    @Inject(
        method = "extractHumanoidRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FLnet/minecraft/client/renderer/item/ItemModelResolver;)V",
        at = @At("RETURN")
    )
    private static void invistags$fillRememberedGear(
        LivingEntity entity,
        HumanoidRenderState state,
        float partialTicks,
        ItemModelResolver itemModelResolver,
        CallbackInfo ci
    ) {
        if (!ModConfig.get(Toggle.GEAR)) {
            return;
        }
        if (entity instanceof Player player && player.isInvisible()) {
            GearReveal.decorate(player, state, itemModelResolver);
        }
    }
}
