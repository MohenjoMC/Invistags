package org.codersoft.cleaspfabric.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;

/**
 * A system toast whose icon is the player's head instead of the vanilla system icon.
 *
 * <p>{@link SystemToast} draws its background into local coordinates and lets the
 * toast manager translate the matrix, so the head can simply be drawn on top of the
 * 17-pixel icon area at the left edge.
 */
public final class InvisiblePlayerToast extends SystemToast {

    private static final long DISPLAY_TIME_MS = 5000L;
    private static final int ICON_X = 1;
    private static final int ICON_Y = 8;
    private static final int ICON_SIZE = 16;

    private final SkinTextures skin;

    public InvisiblePlayerToast(SkinTextures skin, Text title, Text description) {
        super(new Type(DISPLAY_TIME_MS), title, description);
        this.skin = skin;
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {
        super.draw(context, textRenderer, startTime);
        if (skin != null) {
            PlayerSkinDrawer.draw(context, skin, ICON_X, ICON_Y, ICON_SIZE);
        }
    }
}
