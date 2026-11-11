package org.codersoft.cleaspfabric.client;

import org.codersoft.cleaspfabric.NametagPolicy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NametagPolicyTest {

    private static NametagPolicy.Subject player(boolean invisible, boolean self) {
        return NametagPolicy.Subject.player(invisible, self);
    }

    private static NametagPolicy.Subject mob(boolean invisible, boolean customNameVisible) {
        return NametagPolicy.Subject.mob(invisible, customNameVisible);
    }

    @Test
    void invisibleOtherPlayerIsForcedUprightWhenNametagsAreOn() {
        NametagPolicy.Decision decision = NametagPolicy.decide(player(true, false), true, false);

        assertTrue(decision.showLabel());
        assertTrue(decision.seeThrough());
    }

    @Test
    void ownPlayerIsNeverForced() {
        NametagPolicy.Decision decision = NametagPolicy.decide(player(true, true), true, true);

        assertFalse(decision.showLabel());
        assertFalse(decision.seeThrough());
    }

    @Test
    void visiblePlayerIsLeftToVanilla() {
        assertFalse(NametagPolicy.decide(player(false, false), true, true).showLabel());
    }

    @Test
    void nametagsOffMeansNoPlayerLabelEvenWithMobsOn() {
        assertFalse(NametagPolicy.decide(player(true, false), false, true).showLabel());
    }

    @Test
    void customNameVisibleInvisibleMobFollowsTheMobToggle() {
        NametagPolicy.Decision shown = NametagPolicy.decide(mob(true, true), false, true);
        NametagPolicy.Decision hidden = NametagPolicy.decide(mob(true, true), true, false);

        assertTrue(shown.showLabel());
        assertFalse(shown.seeThrough(), "mobs do not get the player-only through-block copy");
        assertFalse(hidden.showLabel());
    }

    @Test
    void invisibleMobWithoutAVisibleCustomNameIsNeverLabelled() {
        assertFalse(NametagPolicy.decide(mob(true, false), true, true).showLabel());
    }

    @Test
    void visibleMobIsLeftToVanilla() {
        assertFalse(NametagPolicy.decide(mob(false, true), true, true).showLabel());
    }

    @Test
    void mobToggleIsIndependentOfTheNametagToggle() {
        assertTrue(NametagPolicy.decide(mob(true, true), false, true).showLabel());
        assertTrue(NametagPolicy.decide(player(true, false), true, false).showLabel());
    }
}
