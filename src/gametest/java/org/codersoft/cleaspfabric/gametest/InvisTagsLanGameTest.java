package org.codersoft.cleaspfabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.Toggle;

import java.util.Properties;

/**
 * Two-player test against a dedicated server bound to all interfaces.
 *
 * <p>Starts a dedicated server on this machine, joins it with the in-process client and
 * waits for a second player ({@code INVISTAGS_LAN_BOT}, e.g. a siftbot instance on another
 * machine) to connect over the network. The second player is given gear while visible, turned
 * invisible, then stripped by the server — the exact sequence gear reveal exists for.
 *
 * <p>Requires an external bot, so it is skipped unless {@code INVISTAGS_LAN_BOT} is set.
 * The dedicated server listens on 25565/0.0.0.0 with offline auth.
 */
public final class InvisTagsLanGameTest implements FabricClientGameTest {

    private static final String BOT_ENV = "INVISTAGS_LAN_BOT";
    private static final int BOT_JOIN_TIMEOUT_TICKS = 3600;

    @Override
    public void runTest(ClientGameTestContext context) {
        String bot = System.getenv(BOT_ENV);
        if (bot == null || bot.isBlank()) {
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

        try (TestDedicatedServerContext server = context.worldBuilder().createServer(properties);
             TestServerConnection connection = server.connect()) {
            connection.getClientWorld().waitForChunksRender();
            context.restoreDefaultGameOptions();

            context.runOnClient(client -> {
                ModConfig.set(Toggle.SKIN, true);
                ModConfig.set(Toggle.NAMETAG, true);
                ModConfig.set(Toggle.GEAR, true);
                ModConfig.set(Toggle.TOAST, true);
            });

            context.waitFor(client -> playerListContains(client, bot), BOT_JOIN_TIMEOUT_TICKS);
            context.waitTicks(20);

            server.runCommand("time set day");
            server.runCommand("execute at @p run tp " + bot + " ^ ^ ^4");
            giveGear(server, bot);
            context.waitTicks(20);
            context.takeScreenshot("lan-bot-visible");

            // The client has now cached the bot's gear. Invisibility plus a server that
            // strips equipment is what gear reveal has to undo.
            server.runCommand("effect give " + bot + " minecraft:invisibility 1200 0 true");
            context.waitTicks(20);
            if (!context.computeOnClient(client -> {
                PlayerEntity entity = botEntity(client, bot);
                return entity != null && entity.isInvisible();
            })) {
                throw new AssertionError(bot + " never became invisible on the client");
            }
            context.takeScreenshot("lan-bot-invisible");

            // The server strips the equipment. Gear reveal should still show what the
            // client cached; turning the toggle off proves the gear came from the cache.
            stripGear(server, bot);
            context.waitTicks(20);
            context.takeScreenshot("lan-bot-stripped-gear-on");

            context.runOnClient(client -> ModConfig.set(Toggle.GEAR, false));
            context.waitTicks(20);
            context.takeScreenshot("lan-bot-stripped-gear-off");

            context.waitFor(client -> playerListContains(client, bot), BOT_JOIN_TIMEOUT_TICKS);
            if (!context.computeOnClient(client -> botEntity(client, bot) != null)) {
                throw new AssertionError("Two-player world never contained " + bot);
            }
        }
    }

    private static void giveGear(TestDedicatedServerContext server, String bot) {
        replace(server, bot, "armor.head", "minecraft:diamond_helmet");
        replace(server, bot, "armor.chest", "minecraft:diamond_chestplate");
        replace(server, bot, "armor.legs", "minecraft:diamond_leggings");
        replace(server, bot, "armor.feet", "minecraft:diamond_boots");
        replace(server, bot, "weapon.mainhand", "minecraft:diamond_sword");
    }

    private static void stripGear(TestDedicatedServerContext server, String bot) {
        replace(server, bot, "armor.head", "minecraft:air");
        replace(server, bot, "armor.chest", "minecraft:air");
        replace(server, bot, "armor.legs", "minecraft:air");
        replace(server, bot, "armor.feet", "minecraft:air");
        replace(server, bot, "weapon.mainhand", "minecraft:air");
    }

    private static void replace(
        TestDedicatedServerContext server,
        String bot,
        String slot,
        String item
    ) {
        server.runCommand("item replace entity " + bot + " " + slot + " with " + item);
    }

    private static boolean playerListContains(MinecraftClient client, String name) {
        if (client.getNetworkHandler() == null) {
            return false;
        }
        for (PlayerListEntry entry : client.getNetworkHandler().getPlayerList()) {
            if (name.equals(entry.getProfile().name())) {
                return true;
            }
        }
        return false;
    }

    private static PlayerEntity botEntity(MinecraftClient client, String name) {
        if (client.world == null) {
            return null;
        }
        for (PlayerEntity player : client.world.getPlayers()) {
            if (name.equals(player.getName().getString())) {
                return player;
            }
        }
        return null;
    }
}
