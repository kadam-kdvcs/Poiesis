package org.kdvcs.poiesis.screen.layout;

import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;

public final class AdsorptionColumnGuiLayout {
    public static final int FLUID_INPUT_X = 44;
    public static final int FLUID_INPUT_Y = 17;
    public static final int ADSORBENT_X = 44;
    public static final int ADSORBENT_Y = 57;
    public static final int FLUID_OUTPUT_X = 134;
    public static final int FLUID_OUTPUT_Y = 28;
    public static final int RESULT_X = 134;
    public static final int RESULT_Y = 46;
    public static final int FLUID_SIZE = 16;

    public static final int BATTERY_X = 8;
    public static final int BATTERY_Y = 62;
    public static final int PLAYER_INVENTORY_X = 8;
    public static final int PLAYER_INVENTORY_Y = 93;

    public static final int PROGRESS_X = 64;
    public static final int PROGRESS_Y = 24;
    public static final int PROGRESS_WIDTH = 68;
    public static final int PROGRESS_HEIGHT = 45;

    public static final ProgressIndicatorLayout PROGRESS =
            new ProgressIndicatorLayout(
                    new ProgressIndicatorLayout.TextureSpec(
                            widget("adsorber_base"),
                            PROGRESS_WIDTH,
                            PROGRESS_HEIGHT
                    ),
                    new ProgressIndicatorLayout.TextureSpec(
                            widget("adsorber_progress_arrow"),
                            PROGRESS_WIDTH,
                            PROGRESS_HEIGHT
                    ),
                    PROGRESS_X,
                    PROGRESS_Y,
                    new ProgressIndicatorLayout.SpriteRegion(
                            0, 0, PROGRESS_WIDTH, PROGRESS_HEIGHT
                    ),
                    new ProgressIndicatorLayout.SpriteRegion(
                            0, 0, PROGRESS_WIDTH, PROGRESS_HEIGHT
                    )
            );

    private static ResourceLocation widget(String name) {
        return new ResourceLocation(
                Poiesis.MOD_ID,
                "textures/container/widget/" + name + ".png"
        );
    }

    private AdsorptionColumnGuiLayout() {
    }
}
