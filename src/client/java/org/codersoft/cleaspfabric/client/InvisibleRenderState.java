package org.codersoft.cleaspfabric.client;

/**
 * Carries the entity's extracted invisibility on its render state, so a later render step
 * can still tell an invisible entity apart after the reveal temporarily clears
 * {@code state.invisible}.
 */
public interface InvisibleRenderState {

    boolean invistags$wasInvisible();

    void invistags$setWasInvisible(boolean value);
}
