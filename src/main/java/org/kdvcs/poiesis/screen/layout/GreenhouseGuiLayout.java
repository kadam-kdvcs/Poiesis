package org.kdvcs.poiesis.screen.layout;

import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;

public final class GreenhouseGuiLayout {
    public static final int BATTERY_X = 8;
    public static final int BATTERY_Y = 66;

    public static final int INPUT_X = 48;
    public static final int INPUT_Y = 26;
    public static final int INPUT_COLUMNS = 2;
    public static final int INPUT_ROWS = 2;
    public static final int SLOT_SIZE = 18;

    public static final int NUTRIENT_ITEM_X = 102;
    public static final int NUTRIENT_ITEM_Y = 62;

    public static final int WATER_X = 120;
    public static final int WATER_Y = 62;
    public static final int NUTRIENT_FLUID_X = 138;
    public static final int NUTRIENT_FLUID_Y = 62;
    public static final int FLUID_OUTPUT_X = 183;
    public static final int FLUID_OUTPUT_Y = 45;
    public static final int FLUID_SIZE = 16;

    public static final int FIRST_OUTPUT_X = 174;
    public static final int FIRST_OUTPUT_Y = 27;
    public static final int SECOND_OUTPUT_X = 192;
    public static final int SECOND_OUTPUT_Y = 27;

    public static final int PROCESS_MODULE_X = 232;
    public static final int PROCESS_MODULE_Y = 23;
    public static final int FIRST_PERFORMANCE_X = 232;
    public static final int FIRST_PERFORMANCE_Y = 41;
    public static final int SECOND_PERFORMANCE_X = 232;
    public static final int SECOND_PERFORMANCE_Y = 59;

    public static final int PLAYER_INVENTORY_X = 48;
    public static final int PLAYER_INVENTORY_Y = 97;
    public static final int PLAYER_HOTBAR_Y = 155;

    public static final ProgressIndicatorLayout PROGRESS = new ProgressIndicatorLayout(
            new ProgressIndicatorLayout.TextureSpec(widget("greenhouse_base"), 84, 33),
            new ProgressIndicatorLayout.TextureSpec(widget("greenhouse_progress_arrow"), 84, 33),
            86,
            27,
            new ProgressIndicatorLayout.SpriteRegion(0, 0, 84, 33),
            new ProgressIndicatorLayout.SpriteRegion(0, 0, 84, 33)
    );

    private static ResourceLocation widget(String name) {
        return new ResourceLocation(Poiesis.MOD_ID,
                "textures/container/widget/" + name + ".png");
    }

    private GreenhouseGuiLayout() { }
}
