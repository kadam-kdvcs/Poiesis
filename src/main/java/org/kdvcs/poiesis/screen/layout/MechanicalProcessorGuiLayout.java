package org.kdvcs.poiesis.screen.layout;

import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.machine.module.MechanicalMode;

/**
 * Shared visual metadata for the Mechanical Processor's process indicator.
 *
 * Each specialization keeps its own base/progress UVs, while the layered
 * rendering mechanics are shared through ProgressIndicatorLayout.
 */
public final class MechanicalProcessorGuiLayout {

    private static final ProgressIndicatorLayout.TextureSpec BASE_TEXTURE =
            new ProgressIndicatorLayout.TextureSpec(
                    new ResourceLocation(
                            Poiesis.MOD_ID,
                            "textures/container/widget/mechanical_processor_base.png"
                    ),
                    22,
                    48
            );

    private static final ProgressIndicatorLayout.TextureSpec PROGRESS_TEXTURE =
            new ProgressIndicatorLayout.TextureSpec(
                    new ResourceLocation(
                            Poiesis.MOD_ID,
                            "textures/container/widget/mechanical_processor_progress_arrows.png"
                    ),
                    22,
                    48
            );

    private static final ProgressIndicatorLayout PRESSING =
            new ProgressIndicatorLayout(
                    BASE_TEXTURE,
                    PROGRESS_TEXTURE,
                    78,
                    35,
                    new ProgressIndicatorLayout.SpriteRegion(
                            0,
                            0,
                            22,
                            15
                    ),
                    new ProgressIndicatorLayout.SpriteRegion(
                            0,
                            0,
                            22,
                            15
                    )
            );

    private static final ProgressIndicatorLayout EXTRUSION =
            new ProgressIndicatorLayout(
                    BASE_TEXTURE,
                    PROGRESS_TEXTURE,
                    79,
                    34,
                    new ProgressIndicatorLayout.SpriteRegion(
                            0,
                            15,
                            20,
                            15
                    ),
                    new ProgressIndicatorLayout.SpriteRegion(
                            0,
                            15,
                            20,
                            15
                    )
            );

    private static final ProgressIndicatorLayout GRINDING =
            new ProgressIndicatorLayout(
                    BASE_TEXTURE,
                    PROGRESS_TEXTURE,
                    79,
                    34,
                    new ProgressIndicatorLayout.SpriteRegion(
                            0,
                            31,
                            19,
                            16
                    ),
                    new ProgressIndicatorLayout.SpriteRegion(
                            0,
                            31,
                            19,
                            17
                    )
            );

    public static ProgressIndicatorLayout progressVisual(
            MechanicalMode mode
    ) {
        return switch (mode) {
            case PRESSING -> PRESSING;
            case GRINDING -> GRINDING;
            case EXTRUSION -> EXTRUSION;
            case NONE -> null;
        };
    }

    private MechanicalProcessorGuiLayout() {
    }
}
