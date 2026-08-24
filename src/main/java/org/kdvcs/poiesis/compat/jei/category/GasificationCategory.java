package org.kdvcs.poiesis.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.compat.jei.util.JeiStacks;
import org.kdvcs.poiesis.machine.process.GasifierProcess;
import org.kdvcs.poiesis.recipe.GasificationRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.screen.layout.BiomassGasifierGuiLayout;

import java.util.List;

/** Compact JEI view of the gasifier's temperature-driven process area. */
public final class GasificationCategory
        implements IRecipeCategory<GasificationRecipe> {
    private static final int ORIGIN_X = 8;
    private static final int ORIGIN_Y = 13;
    private static final int WIDTH = 146;
    private static final int HEIGHT = 61;

    private static final int INPUT_X = BiomassGasifierGuiLayout.INPUT_X - ORIGIN_X;
    private static final int INPUT_Y = BiomassGasifierGuiLayout.INPUT_Y - ORIGIN_Y;
    private static final int OUTPUT_X = BiomassGasifierGuiLayout.OUTPUT_X - ORIGIN_X;
    private static final int OUTPUT_Y = BiomassGasifierGuiLayout.OUTPUT_Y - ORIGIN_Y;
    private static final int GAS_X = BiomassGasifierGuiLayout.GAS_X - ORIGIN_X;
    private static final int GAS_Y = BiomassGasifierGuiLayout.GAS_Y - ORIGIN_Y;
    private static final int FIRE_X = BiomassGasifierGuiLayout.FIRE_X - ORIGIN_X;
    private static final int FIRE_Y = BiomassGasifierGuiLayout.FIRE_Y - ORIGIN_Y;
    private static final int TEMPERATURE_X = BiomassGasifierGuiLayout.TEMPERATURE_X - ORIGIN_X;
    private static final int TEMPERATURE_Y = BiomassGasifierGuiLayout.TEMPERATURE_Y - ORIGIN_Y;

    private static final ResourceLocation FIRE_TEXTURE = widget("fire");
    private static final ResourceLocation FIRE_BASE_TEXTURE = widget("fire_base");
    private static final ResourceLocation TEMPERATURE_TEXTURE = widget("temperature_bar");
    private static final ResourceLocation TEMPERATURE_BASE_TEXTURE =
            widget("temperature_bar_base");
    private static final ResourceLocation FLUID_OVERLAY_TEXTURE = widget("fluid_slot");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable fluidOverlay;
    private final IDrawable fireBase;
    private final IDrawable temperatureBase;
    private final IDrawableAnimated fire;

    public GasificationCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        icon = guiHelper.createDrawableItemStack(
                ModBlocks.BIOMASS_GASIFIER.get().asItem().getDefaultInstance()
        );
        fluidOverlay = guiHelper.drawableBuilder(
                        FLUID_OVERLAY_TEXTURE,
                        0,
                        0,
                        BiomassGasifierGuiLayout.GAS_SIZE,
                        BiomassGasifierGuiLayout.GAS_SIZE
                )
                .setTextureSize(
                        BiomassGasifierGuiLayout.GAS_SIZE,
                        BiomassGasifierGuiLayout.GAS_SIZE
                )
                .build();
        fireBase = guiHelper.drawableBuilder(
                        FIRE_BASE_TEXTURE,
                        0,
                        0,
                        13,
                        13
                )
                .setTextureSize(13, 13)
                .build();
        temperatureBase = guiHelper.drawableBuilder(
                        TEMPERATURE_BASE_TEXTURE,
                        0,
                        0,
                        10,
                        54
                )
                .setTextureSize(10, 54)
                .build();
        IDrawableStatic staticFire = guiHelper.drawableBuilder(
                        FIRE_TEXTURE,
                        0,
                        0,
                        BiomassGasifierGuiLayout.FIRE_SIZE,
                        BiomassGasifierGuiLayout.FIRE_SIZE
                )
                .setTextureSize(
                        BiomassGasifierGuiLayout.FIRE_SIZE,
                        BiomassGasifierGuiLayout.FIRE_SIZE
                )
                .build();
        fire = guiHelper.createAnimatedDrawable(
                staticFire,
                100,
                IDrawableAnimated.StartDirection.TOP,
                true
        );
    }

    @Override
    public RecipeType<GasificationRecipe> getRecipeType() {
        return PoiesisJeiRecipeTypes.GASIFICATION;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.poiesis.category.gasifier");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            GasificationRecipe recipe,
            IFocusGroup focuses
    ) {
        builder.addInputSlot(INPUT_X, INPUT_Y)
                .setStandardSlotBackground()
                .setSlotName("input")
                .addItemStacks(JeiStacks.displayStacks(recipe.input()));

        ItemStack result = recipe.result();
        if (!result.isEmpty()) {
            builder.addOutputSlot(OUTPUT_X, OUTPUT_Y)
                    .setStandardSlotBackground()
                    .setSlotName("solid_output")
                    .addItemStack(result);
        }

        FluidStack gas = recipe.gasOutput();
        builder.addOutputSlot(GAS_X, GAS_Y)
                .setStandardSlotBackground()
                .setFluidRenderer(gas.getAmount(), false, 16, 16)
                .setOverlay(fluidOverlay, 0, 0)
                .setSlotName("gas_output")
                .addFluidStack(gas.getFluid(), gas.getAmount(), gas.getTag());
    }

    @Override
    public void draw(
            GasificationRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        temperatureBase.draw(
                graphics,
                TEMPERATURE_X - 1,
                TEMPERATURE_Y - 1
        );
        drawTemperature(graphics, recipe.minimumTemperature());
        fireBase.draw(graphics, FIRE_X, FIRE_Y);
        fire.draw(graphics, FIRE_X, FIRE_Y);
    }

    private static void drawTemperature(GuiGraphics graphics, int temperature) {
        int height = Math.max(
                1,
                Math.min(
                        BiomassGasifierGuiLayout.TEMPERATURE_HEIGHT,
                        temperature * BiomassGasifierGuiLayout.TEMPERATURE_HEIGHT
                                / GasifierProcess.MAX_TEMPERATURE
                )
        );
        int sourceY = BiomassGasifierGuiLayout.TEMPERATURE_HEIGHT - height;
        graphics.blit(
                TEMPERATURE_TEXTURE,
                TEMPERATURE_X,
                TEMPERATURE_Y + sourceY,
                0,
                sourceY,
                BiomassGasifierGuiLayout.TEMPERATURE_WIDTH,
                height,
                BiomassGasifierGuiLayout.TEMPERATURE_WIDTH,
                BiomassGasifierGuiLayout.TEMPERATURE_HEIGHT
        );
    }

    @Override
    public List<Component> getTooltipStrings(
            GasificationRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY
    ) {
        if (contains(
                mouseX,
                mouseY,
                FIRE_X,
                FIRE_Y,
                BiomassGasifierGuiLayout.FIRE_SIZE,
                BiomassGasifierGuiLayout.FIRE_SIZE
        )) {
            return List.of(Component.translatable(
                    "jei.poiesis.recipe.duration",
                    recipe.duration()
            ));
        }
        if (contains(
                mouseX,
                mouseY,
                TEMPERATURE_X,
                TEMPERATURE_Y,
                BiomassGasifierGuiLayout.TEMPERATURE_WIDTH,
                BiomassGasifierGuiLayout.TEMPERATURE_HEIGHT
        )) {
            return List.of(Component.translatable(
                    "jei.poiesis.recipe.minimum_temperature",
                    recipe.minimumTemperature()
            ));
        }
        return List.of();
    }

    private static boolean contains(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x && mouseX < x + width
                && mouseY >= y && mouseY < y + height;
    }

    private static ResourceLocation widget(String name) {
        return new ResourceLocation(
                Poiesis.MOD_ID,
                "textures/container/widget/" + name + ".png"
        );
    }
}
