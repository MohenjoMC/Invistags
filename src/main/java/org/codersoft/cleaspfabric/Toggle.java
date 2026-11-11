package org.codersoft.cleaspfabric;

/**
 * The single declaration of a user-facing toggle.
 *
 * <p>Everything the mod exposes about a toggle is derived from this catalogue: the
 * {@code /invis} subcommand name, the JSON config key, the startup default, the
 * config-screen title and tooltip translation keys, the one-line status text and the
 * command feedback label. Adding a feature means adding one constant here (plus its
 * storage default and translations); the command, screen, persistence and tests pick
 * it up without another edit.
 */
public enum Toggle {

    SKIN("skin", "showInvisibleSkin", false, "Skin", "Skin visibility", true),
    NAMETAG("nametag", "showInvisibleNametag", false, "Nametag", "Nametag visibility", true),
    MOBS("mobs", "showInvisibleMobs", false, "Mobs", "Mob reveal", true),
    GEAR("gear", "showInvisibleGear", false, "Gear", "Gear reveal", true),
    TOAST("toast", "toastInvisiblePlayers", false, "Toast on invisible players", "Invisible player toast", true),
    REMEMBER("remember", "rememberState", true, "Remember state", "Remember state", false);

    private static final String CONFIG_PREFIX = "config.invistags.";

    private final String commandName;
    private final String configKey;
    private final boolean defaultValue;
    private final String statusLabel;
    private final String feedbackLabel;
    private final boolean persistedWhileRemembering;

    Toggle(String commandName,
           String configKey,
           boolean defaultValue,
           String statusLabel,
           String feedbackLabel,
           boolean persistedWhileRemembering) {
        this.commandName = commandName;
        this.configKey = configKey;
        this.defaultValue = defaultValue;
        this.statusLabel = statusLabel;
        this.feedbackLabel = feedbackLabel;
        this.persistedWhileRemembering = persistedWhileRemembering;
    }

    /** Brigadier literal, e.g. {@code /invis skin}. */
    public String commandName() {
        return commandName;
    }

    /** Key used in {@code invistags.json}; also the suffix of the translation keys. */
    public String configKey() {
        return configKey;
    }

    /** Value used on first launch and by the config screen's reset button. */
    public boolean defaultValue() {
        return defaultValue;
    }

    /** Short label for the {@code /invis} status dump. */
    public String statusLabel() {
        return statusLabel;
    }

    /** Label used in {@code /invis <name>} feedback. */
    public String feedbackLabel() {
        return feedbackLabel;
    }

    /**
     * Whether this value is written to and read from disk only while
     * {@link #REMEMBER} is on. The remember flag itself is always persisted.
     */
    public boolean persistedWhileRemembering() {
        return persistedWhileRemembering;
    }

    /** Cloth Config title key. */
    public String labelKey() {
        return CONFIG_PREFIX + configKey;
    }

    /** Cloth Config tooltip key. */
    public String tooltipKey() {
        return CONFIG_PREFIX + configKey + ".tooltip";
    }
}
