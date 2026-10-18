package org.kdvcs.poiesis.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.compat.jei.util.JeiProgressIndicator;
import org.kdvcs.poiesis.compat.jei.util.JeiStacks;
import org.kdvcs.poiesis.recipe.AdsorptionRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.screen.layout.AdsorptionColumnGuiLayout;

import java.util.List;

/** Compact JEI rendering of the same process geometry used by the machine GUI. */
public final class AdsorptionColumnCategory
        implements IRecipeCategory<AdsorptionRecipe> {
    // The process span is 106px wide inside a 118px category background.
    private static final int ORIGIN_X = 38;
    private static final int ORIGIN_Y = 8;
    private static final int WIDTH = 118;
    private static final int HEIGHT = 69;

    private static final int FLUID_INPUT_X = AdsorptionColumnGuiLayout.FLUID_INPUT_X - ORIGIN_X;
    private static final int FLUID_INPUT_Y = AdsorptionColumnGuiLayout.FLUID_INPUT_Y - ORIGIN_Y;
    private static final int ADSORBENT_X = AdsorptionColumnGuiLayout.ADSORBENT_X - ORIGIN_X;
    private static final int ADSORBENT_Y = AdsorptionColumnGuiLayout.ADSORBENT_Y - ORIGIN_Y;
    private static final int FLUID_OUTPUT_X = AdsorptionColumnGuiLayout.FLUID_OUTPUT_X - ORIGIN_X;
    private static final int FLUID_OUTPUT_Y = AdsorptionColumnGuiLayout.FLUID_OUTPUT_Y - ORIGIN_Y;
    private static final int RESULT_X = AdsorptionColumnGuiLayout.RESULT_X - ORIGIN_X;
    private static final int RESULT_Y = AdsorptionColumnGuiLayout.RESULT_Y - ORIGIN_Y;
    private static final int ARROW_X = AdsorptionColumnGuiLayout.PROGRESS_X - ORIGIN_X;
    private static final int ARROW_Y = AdsorptionColumnGuiLayout.PROGRESS_Y - ORIGIN_Y;

    private static final ResourceLocation FLUID_SLOT_OVERLAY =
            new ResourceLocation(Poiesis.MOD_ID, "textures/container/widget/fluid_slot.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable fluidOverlay;
    private final JeiProgressIndicator progressIndicator;

    public AdsorptionColumnCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        icon = guiHelper.createDrawableItemStack(
                ModBlocks.ADSORPTION_COLUMN_CONTROLLER.get().asItem().getDefaultInstance()
        );
        fluidOverlay = guiHelper.drawableBuilder(FLUID_SLOT_OVERLAY, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
        progressIndicator = JeiProgressIndicator.create(
                guiHelper,
                AdsorptionColumnGuiLayout.PROGRESS,
                40
        );
    }

    @Override
    public RecipeType<AdsorptionRecipe> getRecipeType() {
        return PoiesisJeiRecipeTypes.ADSORPTION;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.poiesis.category.adsorption_column");
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
            AdsorptionRecipe recipe,
            IFocusGroup focuses
    ) {
        var fluidInput = builder.addInputSlot(FLUID_INPUT_X, FLUID_INPUT_Y)
                .setStandardSlotBackground()
                .setFluidRenderer(recipe.fluidInput().amount(), false, 16, 16)
                .setOverlay(fluidOverlay, 0, 0)
                .setSlotName("fluid_input");
        for (FluidStack stack : recipe.fluidInput().displayStacks()) {
            fluidInput.addFluidStack(stack.getFluid(), stack.getAmount(), stack.getTag());
        }

        builder.addInputSlot(ADSORBENT_X, ADSORBENT_Y)
                .setStandardSlotBackground()
                .setSlotName("adsorbent")
                .addItemStacks(JeiStacks.displayStacks(recipe.adsorbent()));

        FluidStack output = recipe.fluidOutput();
        builder.addOutputSlot(FLUID_OUTPUT_X, FLUID_OUTPUT_Y)
                .setStandardSlotBackground()
                .setFluidRenderer(output.getAmount(), false, 16, 16)
                .setOverlay(fluidOverlay, 0, 0)
                .setSlotName("fluid_output")
                .addFluidStack(output.getFluid(), output.getAmount(), output.getTag());

        builder.addOutputSlot(RESULT_X, RESULT_Y)
                .setStandardSlotBackground()
                .setSlotName("result")
                .addItemStack(recipe.result());
    }

    @Override
    public void draw(
            AdsorptionRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        progressIndicator.draw(graphics, ARROW_X, ARROW_Y);
    }

    @Override
    public List<Component> getTooltipStrings(
            AdsorptionRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY
    ) {
        if (!progressIndicator.contains(mouseX, mouseY, ARROW_X, ARROW_Y)) {
            return List.of();
        }
        return List.of(
                Component.translatable("jei.poiesis.recipe.duration", recipe.duration()),
                Component.translatable("jei.poiesis.recipe.base_power", recipe.baseEnergyPerTick()),
                Component.translatable("jei.poiesis.recipe.required_beds", recipe.requiredBeds())
        );
    }
}
