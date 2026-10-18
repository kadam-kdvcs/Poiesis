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
import org.kdvcs.poiesis.recipe.LeachingRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.screen.layout.LeacherGuiLayout;

import java.util.List;

/** Compact JEI layout matching the Leacher GUI's fluid/item geometry. */
public final class LeacherCategory implements IRecipeCategory<LeachingRecipe> {
    // Leave equal horizontal margins around the 106px-wide process layout.
    private static final int ORIGIN_X = LeacherGuiLayout.ITEM_INPUT_X - 6;
    private static final int ORIGIN_Y = 10;
    private static final int WIDTH = 118;
    private static final int HEIGHT = 69;

    private static final int FLUID_INPUT_X = LeacherGuiLayout.FLUID_INPUT_X - ORIGIN_X;
    private static final int FLUID_INPUT_Y = LeacherGuiLayout.FLUID_INPUT_Y - ORIGIN_Y;
    private static final int ITEM_INPUT_X = LeacherGuiLayout.ITEM_INPUT_X - ORIGIN_X;
    private static final int ITEM_INPUT_Y = LeacherGuiLayout.ITEM_INPUT_Y - ORIGIN_Y;
    private static final int FLUID_OUTPUT_X = LeacherGuiLayout.FLUID_OUTPUT_X - ORIGIN_X;
    private static final int FLUID_OUTPUT_Y = LeacherGuiLayout.FLUID_OUTPUT_Y - ORIGIN_Y;
    private static final int ITEM_OUTPUT_X = LeacherGuiLayout.ITEM_OUTPUT_X - ORIGIN_X;
    private static final int ITEM_OUTPUT_Y = LeacherGuiLayout.ITEM_OUTPUT_Y - ORIGIN_Y;
    private static final int PROGRESS_X = LeacherGuiLayout.PROGRESS.x() - ORIGIN_X;
    private static final int PROGRESS_Y = LeacherGuiLayout.PROGRESS.y() - ORIGIN_Y;
    private static final int SINGLE_OUTPUT_OFFSET_Y = 8;

    private static final ResourceLocation FLUID_SLOT_OVERLAY =
            new ResourceLocation(Poiesis.MOD_ID, "textures/container/widget/fluid_slot.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable fluidOverlay;
    private final JeiProgressIndicator progressIndicator;

    public LeacherCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        icon = guiHelper.createDrawableItemStack(ModBlocks.LEACHER.get().asItem().getDefaultInstance());
        fluidOverlay = guiHelper.drawableBuilder(FLUID_SLOT_OVERLAY, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
        progressIndicator = JeiProgressIndicator.create(guiHelper, LeacherGuiLayout.PROGRESS, 40);
    }

    @Override
    public RecipeType<LeachingRecipe> getRecipeType() {
        return PoiesisJeiRecipeTypes.LEACHING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.poiesis.category.leacher");
    }

    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LeachingRecipe recipe, IFocusGroup focuses) {
        var fluidInput = builder.addSlot(RecipeIngredientRole.INPUT, FLUID_INPUT_X, FLUID_INPUT_Y)
                .setStandardSlotBackground()
                .setFluidRenderer(recipe.fluidInput().amount(), false, 16, 16)
                .setOverlay(fluidOverlay, 0, 0)
                .setSlotName("fluid_input");
        for (FluidStack stack : recipe.fluidInput().displayStacks()) {
            fluidInput.addFluidStack(stack.getFluid(), stack.getAmount(), stack.getTag());
        }

        builder.addInputSlot(ITEM_INPUT_X, ITEM_INPUT_Y)
                .setStandardSlotBackground()
                .setSlotName("item_input")
                .addItemStacks(JeiStacks.displayStacks(recipe.input()));

        FluidStack output = recipe.fluidOutput();
        int fluidOutputY = FLUID_OUTPUT_Y
                + (recipe.result().isEmpty() ? SINGLE_OUTPUT_OFFSET_Y : 0);
        builder.addSlot(RecipeIngredientRole.OUTPUT, FLUID_OUTPUT_X, fluidOutputY)
                .setStandardSlotBackground()
                .setFluidRenderer(output.getAmount(), false, 16, 16)
                .setOverlay(fluidOverlay, 0, 0)
                .setSlotName("fluid_output")
                .addFluidStack(output.getFluid(), output.getAmount(), output.getTag());

        if (!recipe.result().isEmpty()) {
            builder.addOutputSlot(ITEM_OUTPUT_X, ITEM_OUTPUT_Y)
                    .setStandardSlotBackground()
                    .setSlotName("item_output")
                    .addItemStack(recipe.result());
        }
    }

    @Override
    public void draw(LeachingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        progressIndicator.draw(graphics, PROGRESS_X, PROGRESS_Y);
    }

    @Override
    public List<Component> getTooltipStrings(LeachingRecipe recipe, IRecipeSlotsView slots,
                                              double mouseX, double mouseY) {
        if (!progressIndicator.contains(mouseX, mouseY, PROGRESS_X, PROGRESS_Y)) {
            return List.of();
        }
        return List.of(
                Component.translatable("jei.poiesis.recipe.duration", recipe.duration()),
                Component.translatable("jei.poiesis.recipe.base_power", recipe.baseEnergyPerTick())
        );
    }
}
