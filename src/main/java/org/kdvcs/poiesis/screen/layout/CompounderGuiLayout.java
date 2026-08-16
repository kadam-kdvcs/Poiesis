package org.kdvcs.poiesis.screen.layout;

import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;

/**
 * Shared Compounder GUI geometry used by the menu, screen and JEI.
 *
 * The recipe-flow coordinates below are the real machine GUI coordinates.
 * JEI normalizes the same geometry into a compact recipe page instead of
 * inventing a second visual arrangement.
 */
public final class CompounderGuiLayout {

    public static final int FIRST_INPUT_X = 37;
    public static final int FIRST_INPUT_Y = 17;

    public static final int SECOND_INPUT_X = 37;
    public static final int SECOND_INPUT_Y = 52;

    public static final int OUTPUT_X = 120;
    public static final int OUTPUT_Y = 35;

    public static final int SPECIALIZATION_X = 152;
    public static final int SPECIALIZATION_Y = 16;

    public static final int FIRST_PERFORMANCE_X = 152;
    public static final int FIRST_PERFORMANCE_Y = 34;

    public static final int SECOND_PERFORMANCE_X = 152;
    public static final int SECOND_PERFORMANCE_Y = 52;

    public static final int BATTERY_X = 8;
    public static final int BATTERY_Y = 59;

    private static final ProgressIndicatorLayout.TextureSpec BASE_TEXTURE =
            new ProgressIndicatorLayout.TextureSpec(
                    new ResourceLocation(
                            Poiesis.MOD_ID,
                            "textures/container/widget/compounder_base.png"
                    ),
                    49,
                    34
            );

    private static final ProgressIndicatorLayout.TextureSpec PROGRESS_TEXTURE =
            new ProgressIndicatorLayout.TextureSpec(
                    new ResourceLocation(
                            Poiesis.MOD_ID,
                            "textures/container/widget/compounder_progress_arrows.png"
                    ),
                    49,
                    34
            );

    public static final ProgressIndicatorLayout PROGRESS =
            new ProgressIndicatorLayout(
                    BASE_TEXTURE,
                    PROGRESS_TEXTURE,
                    60,
                    26,
                    new ProgressIndicatorLayout.SpriteRegion(
                            0,
                            0,
                            49,
                            34
                    ),
                    new ProgressIndicatorLayout.SpriteRegion(
                            0,
                            0,
                            49,
                            34
                    )
            );

    /*
     * Compact JEI recipe-flow bounds.
     *
     * Real GUI process area:
     *   left   = first input X = 37
     *   top    = first input Y = 17
     *   right  = output X + slot size = 138
     *   bottom = second input Y + slot size = 70
     *
     * So the exact process cluster is 101x53. JEI only adds a 4px outer pad.
     */
    public static final int JEI_PADDING = 4;
    public static final int JEI_CORE_LEFT = FIRST_INPUT_X;
    public static final int JEI_CORE_TOP = FIRST_INPUT_Y;
    public static final int JEI_CORE_WIDTH = 101;
    public static final int JEI_CORE_HEIGHT = 53;

    public static final int JEI_WIDTH =
            JEI_CORE_WIDTH + JEI_PADDING * 2;

    public static final int JEI_HEIGHT =
            JEI_CORE_HEIGHT + JEI_PADDING * 2;

    public static int jeiX(
            int machineGuiX
    ) {
        return JEI_PADDING
                + machineGuiX
                - JEI_CORE_LEFT;
    }

    public static int jeiY(
            int machineGuiY
    ) {
        return JEI_PADDING
                + machineGuiY
                - JEI_CORE_TOP;
    }

    private CompounderGuiLayout() {
    }
}
