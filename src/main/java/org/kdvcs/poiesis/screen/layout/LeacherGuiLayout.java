package org.kdvcs.poiesis.screen.layout;

import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;

public final class LeacherGuiLayout {
    public static final int FLUID_INPUT_X = 35;
    public static final int FLUID_INPUT_Y = 28;
    public static final int ITEM_INPUT_X = 35;
    public static final int ITEM_INPUT_Y = 46;
    public static final int FLUID_OUTPUT_X = 125;
    public static final int FLUID_OUTPUT_Y = 28;
    public static final int ITEM_OUTPUT_X = 125;
    public static final int ITEM_OUTPUT_Y = 46;
    public static final int FLUID_SIZE = 16;

    public static final int BATTERY_X = 8;
    public static final int BATTERY_Y = 62;
    public static final int PROCESS_MODULE_X = 152;
    public static final int PROCESS_MODULE_Y = 19;
    public static final int PERFORMANCE_ONE_X = 152;
    public static final int PERFORMANCE_ONE_Y = 37;
    public static final int PERFORMANCE_TWO_X = 152;
    public static final int PERFORMANCE_TWO_Y = 55;
    public static final int PLAYER_INVENTORY_X = 8;
    public static final int PLAYER_INVENTORY_Y = 93;

    public static final ProgressIndicatorLayout PROGRESS = new ProgressIndicatorLayout(
            new ProgressIndicatorLayout.TextureSpec(widget("leacher_base"), 69, 47),
            new ProgressIndicatorLayout.TextureSpec(widget("leacher_progress_arrow"), 69, 47),
            54,
            17,
            new ProgressIndicatorLayout.SpriteRegion(0, 0, 69, 47),
            new ProgressIndicatorLayout.SpriteRegion(0, 0, 69, 47)
    );

    private static ResourceLocation widget(String name) {
        return new ResourceLocation(Poiesis.MOD_ID,
                "textures/container/widget/" + name + ".png");
    }

    private LeacherGuiLayout() { }
}
