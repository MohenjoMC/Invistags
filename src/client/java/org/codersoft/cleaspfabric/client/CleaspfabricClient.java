package org.codersoft.cleaspfabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.Toggle;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CleaspfabricClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("invistags");

    /** The toggles the {@code I} keybind flips together. */
    private static final Set<Toggle> KEYBIND_GROUP =
        EnumSet.of(Toggle.SKIN, Toggle.NAMETAG, Toggle.GEAR);

    private static KeyMapping toggleKey;
    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("InvisTags loaded. Invisible players can no longer hide.");
        ModConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("invistags", "keybinds"));

        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.invistags.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            category
        ));

        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.invistags.config",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GearReveal.tick(client);
            InvisiblePlayerNotifier.tick(client);
            while (toggleKey.consumeClick()) {
                toggleGroup(client);
            }
            while (configKey.consumeClick()) {
                client.setScreenAndShow(InvistagsConfigScreen.create(client.gui.screen()));
            }
        });

        registerCommand();
    }

    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> root =
                ClientCommands.literal("invis");

            root.then(ClientCommands.literal("config")
                .executes(context -> {
                    Minecraft client = Minecraft.getInstance();
                    client.setScreenAndShow(InvistagsConfigScreen.create(client.gui.screen()));
                    return 1;
                })
            );

            for (Toggle toggle : Toggle.values()) {
                root.then(ClientCommands.literal(toggle.commandName())
                    .executes(context -> report(toggle, context.getSource()))
                    .then(ClientCommands.argument("value", BoolArgumentType.bool())
                        .executes(context -> apply(
                            toggle,
                            BoolArgumentType.getBool(context, "value"),
                            context.getSource()))
                    )
                );
            }

            root.executes(context -> status(context.getSource()));
            dispatcher.register(root);
        });
    }

    private static int report(Toggle toggle, FabricClientCommandSource source) {
        source.sendFeedback(Component.literal(
            toggle.feedbackLabel() + ": " + onOff(ModConfig.get(toggle))));
        return 1;
    }

    private static int apply(Toggle toggle, boolean value, FabricClientCommandSource source) {
        ModConfig.set(toggle, value);
        boolean saved = ModConfig.save();
        source.sendFeedback(Component.literal(
            toggle.feedbackLabel() + " set to " + onOff(value) + " "
                + persistenceNote(saved, toggle != Toggle.REMEMBER)));
        return 1;
    }

    private static String persistenceNote(boolean saved, boolean gatedByRemember) {
        if (!saved) {
            return "§c(save failed)";
        }
        return gatedByRemember && !ModConfig.get(Toggle.REMEMBER)
            ? "§7(not saved: remember is off)"
            : "§7(saved)";
    }

    private static int status(FabricClientCommandSource source) {
        source.sendFeedback(Component.literal("§6=== InvisTags Config ==="));
        for (Toggle toggle : Toggle.values()) {
            source.sendFeedback(Component.literal(
                toggle.statusLabel() + ": " + onOff(ModConfig.get(toggle))));
        }
        String names = Arrays.stream(Toggle.values())
            .map(Toggle::commandName)
            .collect(Collectors.joining("|"));
        source.sendFeedback(Component.literal(
            "§7Use /invis " + names + " [true|false], or /invis config"));
        return 1;
    }

    private static String onOff(boolean value) {
        return value ? "§aON" : "§cOFF";
    }

    private static void toggleGroup(Minecraft client) {
        boolean enabled = !KEYBIND_GROUP.stream().allMatch(ModConfig::get);
        KEYBIND_GROUP.forEach(toggle -> ModConfig.set(toggle, enabled));
        ModConfig.save();

        if (client.player != null) {
            client.gui.hud.setOverlayMessage(Component.translatable(
                "message.invistags.toggled",
                Component.translatable(enabled ? "message.invistags.on" : "message.invistags.off")
            ), false);
        }
    }
}
