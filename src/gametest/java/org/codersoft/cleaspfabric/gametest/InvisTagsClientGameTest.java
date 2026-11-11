package org.codersoft.cleaspfabric.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.AABB;
import org.codersoft.cleaspfabric.ModConfig;
import org.codersoft.cleaspfabric.Toggle;
import org.codersoft.cleaspfabric.client.InvisiblePlayerNotifier;
import org.codersoft.cleaspfabric.client.InvistagsConfigScreen;

/**
 * Loads a real singleplayer world so every client renderer and the mixins applied to
 * them are exercised, turns the new toggles on, opens the config screen, runs the
 * /invis command and reveals an invisible custom-named mob.
 */
public final class InvisTagsClientGameTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getClientLevel().waitForChunksRender();
            context.restoreDefaultGameOptions();
            context.runOnClient(client -> {
                client.options.framerateLimit().set(20);
                client.options.renderDistance().set(4);
                client.options.simulationDistance().set(5);
            });

            context.runOnClient(client -> {
                ModConfig.set(Toggle.SKIN, true);
                ModConfig.set(Toggle.NAMETAG, true);
                ModConfig.set(Toggle.GEAR, true);
                ModConfig.set(Toggle.TOAST, true);
            });
            context.waitTicks(20);

            context.runOnClient(client -> client.setScreenAndShow(InvistagsConfigScreen.create(null)));
            context.waitTicks(20);
            if (!context.computeOnClient(client -> client.gui.screen() != null
                && "InvisTags".equals(client.gui.screen().getTitle().getString()))) {
                throw new AssertionError("InvisTags config screen did not open");
            }
            context.takeScreenshot("invistags-config");
            context.runOnClient(client -> client.gui.setScreen(null));

            context.runOnClient(client -> ModConfig.set(Toggle.MOBS, true));
            context.runOnClient(client -> client.player.connection.sendCommand("invis"));
            context.waitTicks(10);
            context.takeScreenshot("invistags-status");

            // An invisible, custom-named mob exercises the shouldShowName injection and
            // NametagPolicy's mob branch on a real render pass.
            singleplayer.getServer().runCommand("time set day");
            singleplayer.getServer().runCommand(
                "execute at @a run summon minecraft:zombie ^ ^ ^3 "
                    + "{CustomName:'Spotted',CustomNameVisible:1b,"
                    + "active_effects:[{id:\"minecraft:invisibility\",duration:1200,show_particles:0b}]}");
            context.waitTicks(20);
            context.takeScreenshot("invistags-invisible-mob");
            if (!context.computeOnClient(InvisTagsClientGameTest::hasInvisibleSpottedZombie)) {
                throw new AssertionError("The invisible custom-named zombie was not in the client level");
            }

            // Render a toast with a real skin so the head + name path is exercised even
            // though a singleplayer world only contains the local player.
            context.runOnClient(client -> InvisiblePlayerNotifier.showPreview(client, client.player));
            context.waitTicks(10);
            context.takeScreenshot("invistags-toast");
        }
    }

    private static boolean hasInvisibleSpottedZombie(net.minecraft.client.Minecraft client) {
        if (client.level == null || client.player == null) {
            return false;
        }
        AABB area = client.player.getBoundingBox().inflate(16.0);
        for (Entity entity : client.level.getEntities(client.player, area)) {
            if (entity.getType() == EntityTypes.ZOMBIE
                && entity instanceof Zombie zombie
                && zombie.isInvisible()
                && zombie.getCustomName() != null
                && "Spotted".equals(zombie.getCustomName().getString())) {
                return true;
            }
        }
        return false;
    }
}
