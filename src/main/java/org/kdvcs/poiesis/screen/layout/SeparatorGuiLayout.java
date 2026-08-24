package org.kdvcs.poiesis.screen.layout;

import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;

public final class SeparatorGuiLayout {
    public static final int SLOT_SIZE = 18;

    public static final int FLUID_X = 37;
    public static final int FLUID_Y = 16;
    public static final int FLUID_SIZE = 16;

    public static final int FIRST_INPUT_X = 37;
    public static final int FIRST_INPUT_Y = 34;
    public static final int SECOND_INPUT_X = 37;
    public static final int SECOND_INPUT_Y = 52;

    public static final int[] OUTPUT_X = {105, 123, 105, 123};
    public static final int[] OUTPUT_Y = {25, 25, 43, 43};

    public static final int SPECIALIZATION_X = CompounderGuiLayout.SPECIALIZATION_X;
    public static final int SPECIALIZATION_Y = CompounderGuiLayout.SPECIALIZATION_Y;
    public static final int FIRST_PERFORMANCE_X = CompounderGuiLayout.FIRST_PERFORMANCE_X;
    public static final int FIRST_PERFORMANCE_Y = CompounderGuiLayout.FIRST_PERFORMANCE_Y;
    public static final int SECOND_PERFORMANCE_X = CompounderGuiLayout.SECOND_PERFORMANCE_X;
    public static final int SECOND_PERFORMANCE_Y = CompounderGuiLayout.SECOND_PERFORMANCE_Y;
    public static final int BATTERY_X = CompounderGuiLayout.BATTERY_X;
    public static final int BATTERY_Y = CompounderGuiLayout.BATTERY_Y;

    private static final ProgressIndicatorLayout.TextureSpec BASE_TEXTURE =
            new ProgressIndicatorLayout.TextureSpec(
                    new ResourceLocation(
                            Poiesis.MOD_ID,
                            "textures/container/widget/separator_base.png"
                    ),
                    20,
                    20
            );

    private static final ProgressIndicatorLayout.TextureSpec PROGRESS_TEXTURE =
            new ProgressIndicatorLayout.TextureSpec(
                    new ResourceLocation(
                            Poiesis.MOD_ID,
                            "textures/container/widget/separator_progress_arrows.png"
                    ),
                    20,
                    21
            );

    public static final ProgressIndicatorLayout PROGRESS =
            new ProgressIndicatorLayout(
                    BASE_TEXTURE,
                    PROGRESS_TEXTURE,
                    69,
                    32,
                    new ProgressIndicatorLayout.SpriteRegion(0, 0, 20, 20),
                    new ProgressIndicatorLayout.SpriteRegion(0, 0, 20, 21)
            );

    /*
     * JEI keeps the Separator's real process layout, normalized around the
     * fluid/input column and the 2x2 output grid with a 4px outer margin.
     */
    public static final int JEI_PADDING = 4;
    public static final int JEI_CORE_LEFT = FLUID_X;
    public static final int JEI_CORE_TOP = FLUID_Y;
    public static final int JEI_CORE_WIDTH =
            OUTPUT_X[1] + SLOT_SIZE - JEI_CORE_LEFT;
    public static final int JEI_CORE_HEIGHT =
            SECOND_INPUT_Y + SLOT_SIZE - JEI_CORE_TOP;
    public static final int JEI_WIDTH =
            JEI_CORE_WIDTH + JEI_PADDING * 2;
    public static final int JEI_HEIGHT =
            JEI_CORE_HEIGHT + JEI_PADDING * 2;

    public static int jeiX(int machineGuiX) {
        return JEI_PADDING + machineGuiX - JEI_CORE_LEFT;
    }

    public static int jeiY(int machineGuiY) {
        return JEI_PADDING + machineGuiY - JEI_CORE_TOP;
    }

    private SeparatorGuiLayout() {
    }
}
