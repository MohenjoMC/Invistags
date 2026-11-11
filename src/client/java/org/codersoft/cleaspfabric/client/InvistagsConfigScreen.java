package org.codersoft.cleaspfabric.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Builds the shared InvisTags config screen. Used both by the Mod Menu
 * integration and by the in-game keybind, so the screen works even when
 * Mod Menu is not installed.
 */
public final class InvistagsConfigScreen {

    private InvistagsConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("config.invistags.title"))
            .setSavingRunnable(ModConfig::save);

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(
            Text.translatable("config.invistags.category.general"));

        general.addEntry(entries
            .startBooleanToggle(
                Text.translatable("config.invistags.showInvisibleSkin"),
                ModConfig.showInvisibleSkin)
            .setDefaultValue(true)
            .setTooltip(Text.translatable("config.invistags.showInvisibleSkin.tooltip"))
            .setSaveConsumer(value -> ModConfig.showInvisibleSkin = value)
            .build());

        general.addEntry(entries
            .startBooleanToggle(
                Text.translatable("config.invistags.showInvisibleNametag"),
                ModConfig.showInvisibleNametag)
            .setDefaultValue(true)
            .setTooltip(Text.translatable("config.invistags.showInvisibleNametag.tooltip"))
            .setSaveConsumer(value -> ModConfig.showInvisibleNametag = value)
            .build());

        general.addEntry(entries
            .startBooleanToggle(
                Text.translatable("config.invistags.showInvisibleMobs"),
                ModConfig.showInvisibleMobs)
            .setDefaultValue(false)
            .setTooltip(Text.translatable("config.invistags.showInvisibleMobs.tooltip"))
            .setSaveConsumer(value -> ModConfig.showInvisibleMobs = value)
            .build());

        general.addEntry(entries
            .startBooleanToggle(
                Text.translatable("config.invistags.showInvisibleGear"),
                ModConfig.showInvisibleGear)
            .setDefaultValue(false)
            .setTooltip(Text.translatable("config.invistags.showInvisibleGear.tooltip"))
            .setSaveConsumer(value -> ModConfig.showInvisibleGear = value)
            .build());

        general.addEntry(entries
            .startBooleanToggle(
                Text.translatable("config.invistags.toastInvisiblePlayers"),
                ModConfig.toastInvisiblePlayers)
            .setDefaultValue(false)
            .setTooltip(Text.translatable("config.invistags.toastInvisiblePlayers.tooltip"))
            .setSaveConsumer(value -> ModConfig.toastInvisiblePlayers = value)
            .build());

        general.addEntry(entries
            .startBooleanToggle(
                Text.translatable("config.invistags.rememberState"),
                ModConfig.rememberState)
            .setDefaultValue(true)
            .setTooltip(Text.translatable("config.invistags.rememberState.tooltip"))
            .setSaveConsumer(value -> ModConfig.rememberState = value)
            .build());

        general.addEntry(entries
            .startTextDescription(Text.translatable("config.invistags.hint"))
            .build());

        return builder.build();
    }
}
