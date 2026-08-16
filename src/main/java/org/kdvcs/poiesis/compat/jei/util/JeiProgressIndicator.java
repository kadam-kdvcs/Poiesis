package org.kdvcs.poiesis.compat.jei.util;

import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import org.kdvcs.poiesis.screen.layout.ProgressIndicatorLayout;

/**
 * Reusable JEI renderer for the same layered progress widgets used by machines.
 *
 * Small widget textures always carry their real texture dimensions through
 * drawableBuilder().setTextureSize(), avoiding JEI's 256x256 default.
 */
public final class JeiProgressIndicator {

    private final IDrawableStatic base;
    private final IDrawableAnimated progress;
    private final int width;
    private final int height;

    private JeiProgressIndicator(
            IDrawableStatic base,
            IDrawableAnimated progress,
            int width,
            int height
    ) {
        this.base = base;
        this.progress = progress;
        this.width = width;
        this.height = height;
    }

    public static JeiProgressIndicator create(
            IGuiHelper guiHelper,
            ProgressIndicatorLayout layout,
            int cycleTicks
    ) {
        ProgressIndicatorLayout.TextureSpec baseTexture =
                layout.baseTexture();

        ProgressIndicatorLayout.SpriteRegion baseRegion =
                layout.base();

        IDrawableStatic base =
                guiHelper.drawableBuilder(
                                baseTexture.location(),
                                baseRegion.u(),
                                baseRegion.v(),
                                baseRegion.width(),
                                baseRegion.height()
                        )
                        .setTextureSize(
                                baseTexture.width(),
                                baseTexture.height()
                        )
                        .build();

        ProgressIndicatorLayout.TextureSpec progressTexture =
                layout.progressTexture();

        ProgressIndicatorLayout.SpriteRegion progressRegion =
                layout.progress();

        IDrawableStatic progressDrawable =
                guiHelper.drawableBuilder(
                                progressTexture.location(),
                                progressRegion.u(),
                                progressRegion.v(),
                                progressRegion.width(),
                                progressRegion.height()
                        )
                        .setTextureSize(
                                progressTexture.width(),
                                progressTexture.height()
                        )
                        .build();

        IDrawableAnimated progress =
                guiHelper.createAnimatedDrawable(
                        progressDrawable,
                        Math.max(1, cycleTicks),
                        IDrawableAnimated.StartDirection.LEFT,
                        false
                );

        return new JeiProgressIndicator(
                base,
                progress,
                layout.width(),
                layout.height()
        );
    }

    public void draw(
            GuiGraphics graphics,
            int x,
            int y
    ) {
        base.draw(
                graphics,
                x,
                y
        );

        progress.draw(
                graphics,
                x,
                y
        );
    }

    public boolean contains(
            double mouseX,
            double mouseY,
            int x,
            int y
    ) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
