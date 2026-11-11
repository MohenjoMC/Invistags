package org.codersoft.cleaspfabric.mixin.client;

import net.minecraft.client.render.entity.state.LivingEntityRenderState;
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

    @Override
    public boolean invistags$wasInvisible() {
        return invistags$wasInvisible;
    }

    @Override
    public void invistags$setWasInvisible(boolean value) {
        invistags$wasInvisible = value;
    }
}
