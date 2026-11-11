package org.codersoft.cleaspfabric.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.Toggle;

/**
 * Builds the shared InvisTags config screen. Used both by the Mod Menu
 * integration and by the in-game keybind, so the screen works even when
 * Mod Menu is not installed.
 *
 * <p>Every entry comes from {@link Toggle}, so the screen cannot fall out of
 * step with the command or the config file.
 */
public final class InvistagsConfigScreen {

    private InvistagsConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.translatable("config.invistags.title"))
            .setSavingRunnable(ModConfig::save);

        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(
            Component.translatable("config.invistags.category.general"));

        for (Toggle toggle : Toggle.values()) {
            general.addEntry(entries
                .startBooleanToggle(
                    Component.translatable(toggle.labelKey()),
                    ModConfig.get(toggle))
                .setDefaultValue(toggle.defaultValue())
                .setTooltip(Component.translatable(toggle.tooltipKey()))
                .setSaveConsumer(value -> ModConfig.set(toggle, value))
                .build());
        }

        general.addEntry(entries
            .startTextDescription(Component.translatable("config.invistags.hint"))
            .build());

        return builder.build();
    }
}
