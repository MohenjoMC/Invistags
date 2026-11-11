package org.codersoft.cleaspfabric;

/**
 * Decides whether the mod forces a nametag to render, and whether its pose needs
 * correcting. One rule, read by both label mixins, instead of each injection
 * re-deriving "invisible and enabled".
 *
 * <p>Kept free of Minecraft types so it can be unit tested without launching a client.
 */
public final class NametagPolicy {

    private NametagPolicy() {
    }

    /**
     * What kind of entity the label belongs to, as far as the rule cares.
     *
     * @param player      a player entity rather than a mob
     * @param invisible   currently has the invisibility effect
     * @param self        the local player
     * @param customNamed has a custom name (mobs only)
     */
    public record Subject(boolean player, boolean invisible, boolean self, boolean customNamed) {

        public static Subject player(boolean invisible, boolean self) {
            return new Subject(true, invisible, self, false);
        }

        public static Subject mob(boolean invisible, boolean customNamed) {
            return new Subject(false, invisible, false, customNamed);
        }
    }

    /**
     * @param showLabel force the label to render even though vanilla would hide it
     * @param seeThrough also submit the through-block copy of the label
     */
    public record Decision(boolean showLabel, boolean seeThrough) {

        public static final Decision HIDDEN = new Decision(false, false);

        public static Decision shown(boolean seeThrough) {
            return new Decision(true, seeThrough);
        }
    }

    /**
     * @param subject        the entity whose label is being considered
     * @param playersEnabled the nametag toggle
     * @param mobsEnabled    the mob-reveal toggle
     */
    public static Decision decide(Subject subject, boolean playersEnabled, boolean mobsEnabled) {
        if (!subject.invisible()) {
            return Decision.HIDDEN;
        }
        if (subject.player()) {
            if (subject.self() || !playersEnabled) {
                return Decision.HIDDEN;
            }
            return Decision.shown(true);
        }
        if (!mobsEnabled || !subject.customNamed()) {
            return Decision.HIDDEN;
        }
        return Decision.shown(false);
    }
}
