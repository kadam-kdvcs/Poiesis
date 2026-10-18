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
import net.minecraftforge.fluids.FluidStack;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.recipe.GasGenerationRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.screen.layout.GasGeneratorGuiLayout;

import java.util.List;

/** Compact JEI view of fluid fuel combustion in the Gas Generator. */
public final class GasGeneratorCategory
        implements IRecipeCategory<GasGenerationRecipe> {

    private static final int WIDTH = 60;
    private static final int HEIGHT = 48;
    private static final int FLUID_X = 22;
    private static final int FLUID_Y = 25;
    private static final int FIRE_X = 23;
    private static final int FIRE_Y = 6;

    private static final ResourceLocation FIRE_TEXTURE = widget("fire");
    private static final ResourceLocation FIRE_BASE_TEXTURE = widget("fire_base");
    private static final ResourceLocation FLUID_OVERLAY_TEXTURE = widget("fluid_slot");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable fluidOverlay;
    private final IDrawable fireBase;
    private final IDrawableAnimated fire;

    public GasGeneratorCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        icon = guiHelper.createDrawableItemStack(
                ModBlocks.GAS_GENERATOR.get().asItem().getDefaultInstance()
        );
        fluidOverlay = guiHelper.drawableBuilder(
                        FLUID_OVERLAY_TEXTURE,
                        0,
                        0,
                        GasGeneratorGuiLayout.FLUID_SIZE,
                        GasGeneratorGuiLayout.FLUID_SIZE
                )
                .setTextureSize(
                        GasGeneratorGuiLayout.FLUID_SIZE,
                        GasGeneratorGuiLayout.FLUID_SIZE
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
        IDrawableStatic staticFire = guiHelper.drawableBuilder(
                        FIRE_TEXTURE,
                        0,
                        0,
                        GasGeneratorGuiLayout.FIRE_SIZE,
                        GasGeneratorGuiLayout.FIRE_SIZE
                )
                .setTextureSize(
                        GasGeneratorGuiLayout.FIRE_SIZE,
                        GasGeneratorGuiLayout.FIRE_SIZE
                )
                .build();
        fire = guiHelper.createAnimatedDrawable(
                staticFire,
                40,
                IDrawableAnimated.StartDirection.TOP,
                true
        );
    }

    @Override
    public RecipeType<GasGenerationRecipe> getRecipeType() {
        return PoiesisJeiRecipeTypes.GAS_GENERATION;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.poiesis.category.gas_generator");
    }

    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            GasGenerationRecipe recipe,
            IFocusGroup focuses
    ) {
        var fluidInput = builder.addInputSlot(FLUID_X, FLUID_Y)
                .setStandardSlotBackground()
                .setFluidRenderer(recipe.fluid().amount(), false, 16, 16)
                .setOverlay(fluidOverlay, 0, 0)
                .setSlotName("fuel");
        for (FluidStack stack : recipe.fluid().displayStacks()) {
            fluidInput.addFluidStack(
                    stack.getFluid(),
                    stack.getAmount(),
                    stack.getTag()
            );
        }
    }

    @Override
    public void draw(
            GasGenerationRecipe recipe,
            IRecipeSlotsView slots,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        fireBase.draw(graphics, FIRE_X, FIRE_Y);
        fire.draw(graphics, FIRE_X, FIRE_Y);
    }

    @Override
    public List<Component> getTooltipStrings(
            GasGenerationRecipe recipe,
            IRecipeSlotsView slots,
            double mouseX,
            double mouseY
    ) {
        if (!contains(
                mouseX,
                mouseY,
                FIRE_X,
                FIRE_Y,
                GasGeneratorGuiLayout.FIRE_SIZE,
                GasGeneratorGuiLayout.FIRE_SIZE
        )) {
            return List.of();
        }

        long totalEnergy = (long) recipe.fluid().amount() * recipe.energyPerMb();
        return List.of(
                Component.translatable(
                        "jei.poiesis.recipe.energy_per_mb",
                        recipe.energyPerMb()
                ),
                Component.translatable(
                        "jei.poiesis.recipe.generation_rate",
                        recipe.energyPerTick()
                ),
                Component.translatable(
                        "jei.poiesis.recipe.total_energy",
                        totalEnergy
                )
        );
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
