package org.codersoft.cleaspfabric.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerSkin;

/**
 * A system toast whose icon is the player's head instead of the vanilla system icon.
 *
 * <p>{@link SystemToast} draws its background into local coordinates, so the head is
 * blitted over the 16-pixel icon area at the left edge after the vanilla pass.
 */
public final class InvisiblePlayerToast extends SystemToast {

    private static final SystemToast.SystemToastId ID = new SystemToast.SystemToastId(5000L);
    private static final int ICON_X = 1;
    private static final int ICON_Y = 8;
    private static final int ICON_SIZE = 16;

    private final PlayerSkin skin;

    public InvisiblePlayerToast(PlayerSkin skin, Component title, Component message) {
        super(ID, title, message);
        this.skin = skin;
    }

    @Override
    public void extractRenderState(
        GuiGraphicsExtractor graphics,
        Font font,
        long fullyVisibleForMs
    ) {
        super.extractRenderState(graphics, font, fullyVisibleForMs);
        if (skin != null) {
            graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                skin.body().texturePath(),
                ICON_X,
                ICON_Y,
                8.0F,
                8.0F,
                ICON_SIZE,
                ICON_SIZE,
                64,
                64,
                -1
            );
        }
    }
}
