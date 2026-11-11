package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.codersoft.cleaspfabric.client.InvisibleRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Stores the invisibility the entity had when its render state was extracted.
 * See {@link InvisibleRenderState}.
 */
@Mixin(LivingEntityRenderState.class)
public class RenderStateFlagMixin implements InvisibleRenderState {

    @Unique
    private boolean invistags$wasInvisible;

    @Unique
    private boolean invistags$wasInvisibleToPlayer;

    @Override
    public boolean invistags$wasInvisible() {
        return invistags$wasInvisible;
    }

    @Override
    public boolean invistags$wasInvisibleToPlayer() {
        return invistags$wasInvisibleToPlayer;
    }

    @Override
    public void invistags$setWasInvisible(boolean invisible, boolean invisibleToPlayer) {
        invistags$wasInvisible = invisible;
        invistags$wasInvisibleToPlayer = invisibleToPlayer;
    }
}
