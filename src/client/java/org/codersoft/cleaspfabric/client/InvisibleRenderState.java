package org.codersoft.cleaspfabric.client;

/**
 * Carries the entity's extracted invisibility on its render state, so a later render step
 * can still tell an invisible entity apart after the reveal temporarily clears the flags.
 */
public interface InvisibleRenderState {

    boolean invistags$wasInvisible();

    boolean invistags$wasInvisibleToPlayer();

    void invistags$setWasInvisible(boolean invisible, boolean invisibleToPlayer);
}
