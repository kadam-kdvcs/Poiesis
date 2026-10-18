package org.kdvcs.poiesis.screen.layout;

import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;

public final class MicrobialFuelCellGuiLayout {
    public static final int ENERGY_X = 10;
    public static final int ENERGY_BOTTOM_Y = 63;
    public static final int ENERGY_WIDTH = 12;
    public static final int ENERGY_HEIGHT = 45;

    public static final int BATTERY_X = 8;
    public static final int BATTERY_Y = 67;

    public static final int SPECIALIZATION_X = 152;
    public static final int SPECIALIZATION_Y = 21;
    public static final int FIRST_PERFORMANCE_X = 152;
    public static final int FIRST_PERFORMANCE_Y = 39;
    public static final int SECOND_PERFORMANCE_X = 152;
    public static final int SECOND_PERFORMANCE_Y = 57;

    public static final int ANOLYTE_X = 45;
    public static final int ANOLYTE_Y = 43;
    public static final int ANOLYTE_WIDTH = 44;
    public static final int ANOLYTE_HEIGHT = 35;
    public static final int CATHOLYTE_X = 92;
    public static final int CATHOLYTE_Y = 43;
    public static final int CATHOLYTE_WIDTH = 43;
    public static final int CATHOLYTE_HEIGHT = 35;
    public static final int SWAP_X = 86;
    public static final int SWAP_Y = 36;

    public static final int PLAYER_INVENTORY_X = 8;
    public static final int PLAYER_INVENTORY_Y = 98;
    public static final int PLAYER_HOTBAR_Y = 156;

    public static final ProgressIndicatorLayout PROGRESS = new ProgressIndicatorLayout(
            new ProgressIndicatorLayout.TextureSpec(widget("mfc_base"), 84, 18),
            new ProgressIndicatorLayout.TextureSpec(widget("mfc_progress_arrow"), 84, 19),
            50,
            16,
            new ProgressIndicatorLayout.SpriteRegion(0, 0, 84, 18),
            new ProgressIndicatorLayout.SpriteRegion(0, 0, 84, 19)
    );

    private static ResourceLocation widget(String name) {
        return new ResourceLocation(Poiesis.MOD_ID,
                "textures/container/widget/" + name + ".png");
    }

    private MicrobialFuelCellGuiLayout() { }
}
