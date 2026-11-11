package org.codersoft.cleaspfabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.Toggle;
import org.codersoft.cleaspfabric.client.GearReveal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Two-player test against a dedicated server bound to all interfaces.
 *
 * <p>Starts a dedicated server on this machine, joins it with the in-process client and
 * waits for a second player ({@code INVISTAGS_LAN_BOT}, e.g. a siftbot instance on
 * another machine) to connect over the network. The second player is given gear while
 * visible, turned invisible, then stripped by the server, the exact sequence gear reveal
 * exists for. Skipped unless {@code INVISTAGS_LAN_BOT} is set.
 */
public final class InvisTagsLanGameTest implements FabricClientGameTest {

    private static final Logger LOGGER = LoggerFactory.getLogger("invistags-gametest");
    private static final String BOT_ENV = "INVISTAGS_LAN_BOT";
    private static final int BOT_JOIN_TIMEOUT_TICKS = 3600;

    @Override
    public void runTest(ClientGameTestContext context) {
        String bot = System.getenv(BOT_ENV);
        if (bot == null || bot.isBlank()) {
            LOGGER.info("Skipping two-player gear-reveal test: {} is not set", BOT_ENV);
            return;
        }

        Properties properties = new Properties();
        properties.setProperty("server-port", "25565");
        properties.setProperty("server-ip", "0.0.0.0");
        properties.setProperty("online-mode", "false");
        properties.setProperty("enforce-secure-profile", "false");
        properties.setProperty("level-type", "minecraft:flat");
        properties.setProperty("spawn-protection", "0");
        properties.setProperty("difficulty", "peaceful");
        properties.setProperty("pvp", "false");
        properties.setProperty("view-distance", "6");
        properties.setProperty("simulation-distance", "6");
        properties.setProperty("max-players", "4");

        try (TestDedicatedServerContext server = context.worldBuilder().createServer(properties)) {
            TestServerConnection connection = server.connect();
            connection.waitForChunksRender();
            context.restoreDefaultGameOptions();
            context.runOnClient(client -> {
                client.options.framerateLimit().set(20);
                ModConfig.set(Toggle.SKIN, true);
                ModConfig.set(Toggle.NAMETAG, true);
                ModConfig.set(Toggle.GEAR, true);
                ModConfig.set(Toggle.TOAST, true);
            });

            try {
                context.waitFor(client -> playerPresent(client, bot), BOT_JOIN_TIMEOUT_TICKS);
            } catch (AssertionError e) {
                throw new AssertionError("Bot " + bot + " never appeared in the tab list. Is "
                    + BOT_ENV + "=" + bot + " running and pointed at this host?", e);
            }
            context.waitTicks(20);

            server.runCommand("time set day");
            server.runCommand("execute at @a[name=!" + bot + ",limit=1] run tp " + bot + " ^ ^ ^4");
            giveGear(server, bot);
            context.waitTicks(20);
            context.takeScreenshot("lan-bot-visible");

            server.runCommand("effect give " + bot + " minecraft:invisibility 1200 0 true");
            context.waitTicks(20);
            if (!context.computeOnClient(client -> {
                Player entity = botEntity(client, bot);
                return entity != null && entity.isInvisible();
            })) {
                throw new AssertionError(bot + " never became invisible on the client");
            }
            context.takeScreenshot("lan-bot-invisible");

            stripGear(server, bot);
            context.waitTicks(20);
            if (!context.computeOnClient(client -> botEquipmentIsEmpty(client, bot))) {
                throw new AssertionError(bot + " still has live equipment after the server stripped it");
            }
            if (!context.computeOnClient(client -> {
                Player entity = botEntity(client, bot);
                if (entity == null) {
                    return false;
                }
                AvatarRenderState state = new AvatarRenderState();
                GearReveal.decorate(entity, state, new ItemModelResolver(client.getModelManager()));
                return !state.headEquipment.isEmpty()
                    && !state.chestEquipment.isEmpty()
                    && !state.legsEquipment.isEmpty()
                    && !state.feetEquipment.isEmpty()
                    && !state.rightHandItemState.isEmpty()
                    && !state.leftHandItemState.isEmpty();
            })) {
                throw new AssertionError("Gear cache did not fill all six render-state slots for " + bot);
            }
            context.takeScreenshot("lan-bot-stripped-gear-on");

            context.runOnClient(client -> ModConfig.set(Toggle.GEAR, false));
            context.waitTicks(20);
            context.takeScreenshot("lan-bot-stripped-gear-off");
        }
    }

    private static void giveGear(TestDedicatedServerContext server, String bot) {
        replace(server, bot, "armor.head", "minecraft:diamond_helmet");
        replace(server, bot, "armor.chest", "minecraft:diamond_chestplate");
        replace(server, bot, "armor.legs", "minecraft:diamond_leggings");
        replace(server, bot, "armor.feet", "minecraft:diamond_boots");
        replace(server, bot, "weapon.mainhand", "minecraft:diamond_sword");
        replace(server, bot, "weapon.offhand", "minecraft:shield");
    }

    private static void stripGear(TestDedicatedServerContext server, String bot) {
        replace(server, bot, "armor.head", "minecraft:air");
        replace(server, bot, "armor.chest", "minecraft:air");
        replace(server, bot, "armor.legs", "minecraft:air");
        replace(server, bot, "armor.feet", "minecraft:air");
        replace(server, bot, "weapon.mainhand", "minecraft:air");
        replace(server, bot, "weapon.offhand", "minecraft:air");
    }

    private static void replace(
        TestDedicatedServerContext server,
        String bot,
        String slot,
        String item
    ) {
        server.runCommand("item replace entity " + bot + " " + slot + " with " + item);
    }

    private static boolean playerPresent(Minecraft client, String name) {
        return client.getConnection() != null
            && client.getConnection().getListedOnlinePlayers().stream()
                .anyMatch(info -> name.equals(info.getProfile().name()));
    }

    private static Player botEntity(Minecraft client, String name) {
        if (client.level == null) {
            return null;
        }
        for (Player player : client.level.players()) {
            if (name.equals(player.getName().getString())) {
                return player;
            }
        }
        return null;
    }

    private static boolean botEquipmentIsEmpty(Minecraft client, String name) {
        Player entity = botEntity(client, name);
        if (entity == null) {
            return false;
        }
        for (EquipmentSlot slot : new EquipmentSlot[]{
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
            EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND}) {
            if (!entity.getItemBySlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
