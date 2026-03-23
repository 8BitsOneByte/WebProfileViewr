package org.exmple.webprofileviewer.client.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.exmple.webprofileviewer.client.config.ModConfig;

public final class AntiAfkHud {
    private static final Identifier TEXTURE_SMALL = Identifier.parse("webprofileviewer:textures/gui/smallantiafkicon.png");
    private static final Identifier TEXTURE_LARGE = Identifier.parse("webprofileviewer:textures/gui/antiafkicon.png");

    private static final int TEX_SMALL = 16;
    private static final int TEX_LARGE = 64;

    private static final int DRAW_SIZE = 16;

    private AntiAfkHud() {
    }

    public static void render(GuiGraphics context) {
        if (!ModConfig.getInstance().antiAFK) {
            return;
        }
        boolean useSmall = ModConfig.getInstance().antiAfkIconSmall;
        Identifier texture = useSmall ? TEXTURE_SMALL : TEXTURE_LARGE;
        int x = 8;
        int y = (context.guiHeight() - DRAW_SIZE) / 2;
        context.blit(
            RenderPipelines.GUI_TEXTURED,
            texture,
            x,
            y,
            0,
            0,
            DRAW_SIZE,
            DRAW_SIZE,
            DRAW_SIZE,
            DRAW_SIZE
        );
    }
}
