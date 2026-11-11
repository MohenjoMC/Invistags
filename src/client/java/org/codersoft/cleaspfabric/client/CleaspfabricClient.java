package org.codersoft.cleaspfabric.client;

import org.codersoft.cleaspfabric.ModConfig;

import org.codersoft.cleaspfabric.Toggle;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CleaspfabricClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("invisnametag");

    /** The toggles the {@code I} keybind flips together. */
    private static final Set<Toggle> KEYBIND_GROUP =
        EnumSet.of(Toggle.SKIN, Toggle.NAMETAG, Toggle.GEAR);

    private static KeyBinding toggleKey;
    private static KeyBinding configKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("InvisNametag loaded. Invisible players can no longer hide.");
        ModConfig.load();

        KeyBinding.Category category = KeyBinding.Category.create(
            Identifier.of("invistags", "keybinds"));

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.invistags.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            category
        ));

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.invistags.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GearReveal.tick(client);
            InvisiblePlayerNotifier.tick(client);
            while (toggleKey.wasPressed()) {
                toggleGroup(client);
            }
            while (configKey.wasPressed()) {
                client.setScreen(InvistagsConfigScreen.create(client.currentScreen));
            }
        });

        registerCommand();
    }

    private static void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> root =
                ClientCommandManager.literal("invis");

            root.then(ClientCommandManager.literal("config")
                .executes(context -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(InvistagsConfigScreen.create(client.currentScreen));
                    return 1;
                })
            );

            for (Toggle toggle : Toggle.values()) {
                root.then(ClientCommandManager.literal(toggle.commandName())
                    .executes(context -> report(toggle, context.getSource()))
                    .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
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
        source.sendFeedback(Text.literal(
            toggle.feedbackLabel() + ": " + onOff(ModConfig.get(toggle))));
        return 1;
    }

    private static int apply(Toggle toggle, boolean value, FabricClientCommandSource source) {
        ModConfig.set(toggle, value);
        boolean saved = ModConfig.save();
        source.sendFeedback(Text.literal(
            toggle.feedbackLabel() + " set to " + onOff(value) + " "
                + persistenceNote(saved, toggle != Toggle.REMEMBER)));
        return 1;
    }

    private static int status(FabricClientCommandSource source) {
        source.sendFeedback(Text.literal("§6=== InvisTags Config ==="));
        for (Toggle toggle : Toggle.values()) {
            source.sendFeedback(Text.literal(
                toggle.statusLabel() + ": " + onOff(ModConfig.get(toggle))));
        }
        String names = Arrays.stream(Toggle.values())
            .map(Toggle::commandName)
            .collect(Collectors.joining("|"));
        source.sendFeedback(Text.literal(
            "§7Use /invis " + names + " [true|false], or /invis config"));
        return 1;
    }

    private static String onOff(boolean value) {
        return value ? "§aON" : "§cOFF";
    }

    private static String persistenceNote(boolean saved, boolean gatedByRemember) {
        if (!saved) {
            return "§7(save failed)";
        }
        if (gatedByRemember && !ModConfig.get(Toggle.REMEMBER)) {
            return "§7(not saved: remember is off)";
        }
        return "§7(saved)";
    }

    private static void toggleGroup(MinecraftClient client) {
        boolean enabled = !KEYBIND_GROUP.stream().allMatch(ModConfig::get);
        KEYBIND_GROUP.forEach(toggle -> ModConfig.set(toggle, enabled));
        boolean saved = ModConfig.save();

        if (client.player != null) {
            client.player.sendMessage(Text.translatable(
                "message.invistags.toggled",
                Text.translatable(enabled ? "message.invistags.on" : "message.invistags.off")
            ).append(" " + persistenceNote(saved, true)), true);
        }
    }
}
