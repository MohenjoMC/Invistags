package org.codersoft.cleaspfabric.gametest;

import org.codersoft.cleaspfabric.Toggle;

import org.codersoft.cleaspfabric.ModConfig;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.TypeFilter;
import org.codersoft.cleaspfabric.client.InvisiblePlayerNotifier;
import org.codersoft.cleaspfabric.client.InvistagsConfigScreen;

/**
 * Loads a real singleplayer world so every client renderer and the mixins applied
 * to them are exercised, turns the new toggles on, opens the config screen and runs
 * the /invis command. A missing mixin target or a broken config entry fails here.
 */
public final class InvisTagsClientGameTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        // Keep chunk generation inside the harness's 1200-tick world-load budget on
        // software-rendered hosts; the test does not care how far the player can see.
        context.runOnClient(client -> {
            client.options.getViewDistance().setValue(2);
            client.options.getSimulationDistance().setValue(5);
            client.options.getMaxFps().setValue(20);
        });

        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getClientWorld().waitForChunksRender();

            context.runOnClient(client -> {
                ModConfig.set(Toggle.SKIN, true);
                ModConfig.set(Toggle.NAMETAG, true);
                ModConfig.set(Toggle.GEAR, true);
                ModConfig.set(Toggle.TOAST, true);
            });
            context.waitTicks(20);

            context.runOnClient(client -> client.setScreen(InvistagsConfigScreen.create(null)));
            context.waitTicks(20);
            if (!context.computeOnClient(client -> client.currentScreen != null
                && "InvisTags".equals(client.currentScreen.getTitle().getString()))) {
                throw new AssertionError("InvisTags config screen did not open");
            }
            context.takeScreenshot("invistags-config");
            context.runOnClient(client -> client.setScreen(null));

            context.runOnClient(client -> ModConfig.set(Toggle.MOBS, true));
            context.runOnClient(client -> client.player.networkHandler.sendChatCommand("invis"));
            context.waitTicks(10);
            context.takeScreenshot("invistags-status");

            // An invisible, custom-named mob exercises the hasLabel injection and
            // NametagPolicy's mob branch on a real render pass.
            singleplayer.getServer().runCommand("time set day");
            singleplayer.getServer().runCommand(
                "execute at @a run summon minecraft:zombie ^ ^ ^3 "
                    + "{CustomName:'Spotted',CustomNameVisible:1b,"
                    + "active_effects:[{id:\"minecraft:invisibility\",duration:1200,show_particles:0b}]}");
            context.waitTicks(20);
            if (!context.computeOnClient(client -> worldContainsInvisibleZombieNamed(client, "Spotted"))) {
                throw new AssertionError("no invisible zombie named Spotted in the client world");
            }
            context.takeScreenshot("invistags-invisible-mob");

            // Render a toast with a real skin so the head + name path is exercised
            // even though a singleplayer world only contains the local player.
            context.runOnClient(client -> InvisiblePlayerNotifier.showPreview(client, client.player));
            context.waitTicks(10);
            context.takeScreenshot("invistags-toast");
            if (!context.computeOnClient(client -> client.getToastManager() != null)) {
                throw new AssertionError("Toast manager unavailable");
            }
        }
    }

    private static boolean worldContainsInvisibleZombieNamed(MinecraftClient client, String name) {
        if (client.world == null || client.player == null) {
            return false;
        }
        return !client.world.getEntitiesByType(
            TypeFilter.instanceOf(ZombieEntity.class),
            client.player.getBoundingBox().expand(32.0),
            zombie -> zombie.isInvisible()
                && zombie.getCustomName() != null
                && name.equals(zombie.getCustomName().getString())
        ).isEmpty();
    }
}
