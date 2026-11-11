package org.codersoft.cleaspfabric.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
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

public class CleaspfabricClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("invisnametag");

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
                toggleEverything(client);
            }
            while (configKey.wasPressed()) {
                client.setScreen(InvistagsConfigScreen.create(client.currentScreen));
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("invis")
                .then(ClientCommandManager.literal("config")
                    .executes(context -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        client.setScreen(InvistagsConfigScreen.create(client.currentScreen));
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("skin")
                    .executes(context -> {
                        boolean current = ModConfig.showInvisibleSkin;
                        context.getSource().sendFeedback(Text.literal(
                            "Skin visibility: " + (current ? "§aON" : "§cOFF")));
                        return 1;
                    })
                    .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            ModConfig.showInvisibleSkin = value;
                            ModConfig.save();
                            context.getSource().sendFeedback(Text.literal(
                                "Skin visibility set to " + (value ? "§aON" : "§cOFF") + " §7(saved)"));
                            return 1;
                        })
                    )
                )
                .then(ClientCommandManager.literal("nametag")
                    .executes(context -> {
                        boolean current = ModConfig.showInvisibleNametag;
                        context.getSource().sendFeedback(Text.literal(
                            "Nametag visibility: " + (current ? "§aON" : "§cOFF")));
                        return 1;
                    })
                    .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            ModConfig.showInvisibleNametag = value;
                            ModConfig.save();
                            context.getSource().sendFeedback(Text.literal(
                                "Nametag visibility set to " + (value ? "§aON" : "§cOFF") + " §7(saved)"));
                            return 1;
                        })
                    )
                )
                .then(ClientCommandManager.literal("mobs")
                    .executes(context -> {
                        boolean current = ModConfig.showInvisibleMobs;
                        context.getSource().sendFeedback(Text.literal(
                            "Mob reveal: " + (current ? "§aON" : "§cOFF")));
                        return 1;
                    })
                    .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            ModConfig.showInvisibleMobs = value;
                            ModConfig.save();
                            context.getSource().sendFeedback(Text.literal(
                                "Mob reveal set to " + (value ? "§aON" : "§cOFF") + " §7(saved)"));
                            return 1;
                        })
                    )
                )
                .then(ClientCommandManager.literal("gear")
                    .executes(context -> {
                        boolean current = ModConfig.showInvisibleGear;
                        context.getSource().sendFeedback(Text.literal(
                            "Gear reveal: " + (current ? "§aON" : "§cOFF")));
                        return 1;
                    })
                    .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            ModConfig.showInvisibleGear = value;
                            ModConfig.save();
                            context.getSource().sendFeedback(Text.literal(
                                "Gear reveal set to " + (value ? "§aON" : "§cOFF") + " §7(saved)"));
                            return 1;
                        })
                    )
                )
                .then(ClientCommandManager.literal("toast")
                    .executes(context -> {
                        boolean current = ModConfig.toastInvisiblePlayers;
                        context.getSource().sendFeedback(Text.literal(
                            "Invisible player toast: " + (current ? "§aON" : "§cOFF")));
                        return 1;
                    })
                    .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            ModConfig.toastInvisiblePlayers = value;
                            ModConfig.save();
                            context.getSource().sendFeedback(Text.literal(
                                "Invisible player toast set to " + (value ? "§aON" : "§cOFF") + " §7(saved)"));
                            return 1;
                        })
                    )
                )
                .then(ClientCommandManager.literal("remember")
                    .executes(context -> {
                        boolean current = ModConfig.rememberState;
                        context.getSource().sendFeedback(Text.literal(
                            "Remember state: " + (current ? "§aON" : "§cOFF")));
                        return 1;
                    })
                    .then(ClientCommandManager.argument("value", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean value = BoolArgumentType.getBool(context, "value");
                            ModConfig.rememberState = value;
                            ModConfig.save();
                            context.getSource().sendFeedback(Text.literal(
                                "Remember state set to " + (value ? "§aON" : "§cOFF") + " §7(saved)"));
                            return 1;
                        })
                    )
                )
                .executes(context -> {
                    context.getSource().sendFeedback(Text.literal(
                        "§6=== InvisTags Config ==="));
                    context.getSource().sendFeedback(Text.literal(
                        "Skin: " + (ModConfig.showInvisibleSkin ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Text.literal(
                        "Nametag: " + (ModConfig.showInvisibleNametag ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Text.literal(
                        "Mobs: " + (ModConfig.showInvisibleMobs ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Text.literal(
                        "Gear: " + (ModConfig.showInvisibleGear ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Text.literal(
                        "Toast on invisible players: " + (ModConfig.toastInvisiblePlayers ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Text.literal(
                        "Remember state: " + (ModConfig.rememberState ? "§aON" : "§cOFF")));
                    context.getSource().sendFeedback(Text.literal(
                        "§7Use /invis skin|nametag|mobs|gear|toast [true|false], /invis remember [true|false], or /invis config"));
                    return 1;
                })
            );
        });
    }

    private static void toggleEverything(MinecraftClient client) {
        boolean enabled = !(ModConfig.showInvisibleSkin
            && ModConfig.showInvisibleNametag
            && ModConfig.showInvisibleGear);
        ModConfig.showInvisibleSkin = enabled;
        ModConfig.showInvisibleNametag = enabled;
        ModConfig.showInvisibleGear = enabled;
        ModConfig.save();

        if (client.player != null) {
            client.player.sendMessage(Text.translatable(
                "message.invistags.toggled",
                Text.translatable(enabled ? "message.invistags.on" : "message.invistags.off")
            ), true);
        }
    }
}
