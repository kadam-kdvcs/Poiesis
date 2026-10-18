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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.compat.jei.util.JeiProgressIndicator;
import org.kdvcs.poiesis.compat.jei.util.JeiStacks;
import org.kdvcs.poiesis.recipe.GreenhouseRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.screen.layout.GreenhouseGuiLayout;

import java.util.List;

/** Compact JEI view matching the Greenhouse's crop and nutrient layout. */
public final class GreenhouseCategory implements IRecipeCategory<GreenhouseRecipe> {
    // Origin chosen so the complete compact layout is centered in the JEI view.
    private static final int ORIGIN_X = 52;
    private static final int ORIGIN_Y = 18;
    private static final int WIDTH = 172;
    private static final int HEIGHT = 72;

    // Leave a 3px gap between the slot's right edge and the arrow's left edge,
    // and align the slot top with the item-output row.
    private static final int INPUT_X = GreenhouseGuiLayout.PROGRESS.x()
            - GreenhouseGuiLayout.SLOT_SIZE - 3 - ORIGIN_X;
    private static final int INPUT_Y = GreenhouseGuiLayout.FIRST_OUTPUT_Y - ORIGIN_Y;
    private static final int NUTRIENT_ITEM_X = GreenhouseGuiLayout.NUTRIENT_ITEM_X - ORIGIN_X;
    private static final int NUTRIENT_ITEM_Y = GreenhouseGuiLayout.NUTRIENT_ITEM_Y - ORIGIN_Y;
    private static final int WATER_X = GreenhouseGuiLayout.WATER_X - ORIGIN_X;
    private static final int WATER_Y = GreenhouseGuiLayout.WATER_Y - ORIGIN_Y;
    private static final int NUTRIENT_FLUID_X = GreenhouseGuiLayout.NUTRIENT_FLUID_X - ORIGIN_X;
    private static final int NUTRIENT_FLUID_Y = GreenhouseGuiLayout.NUTRIENT_FLUID_Y - ORIGIN_Y;
    private static final int FIRST_OUTPUT_X = GreenhouseGuiLayout.FIRST_OUTPUT_X - ORIGIN_X;
    private static final int FIRST_OUTPUT_Y = GreenhouseGuiLayout.FIRST_OUTPUT_Y - ORIGIN_Y;
    private static final int SECOND_OUTPUT_X = GreenhouseGuiLayout.SECOND_OUTPUT_X - ORIGIN_X;
    private static final int SECOND_OUTPUT_Y = GreenhouseGuiLayout.SECOND_OUTPUT_Y - ORIGIN_Y;
    private static final int FLUID_OUTPUT_X = GreenhouseGuiLayout.FLUID_OUTPUT_X - ORIGIN_X;
    private static final int FLUID_OUTPUT_Y = GreenhouseGuiLayout.FLUID_OUTPUT_Y - ORIGIN_Y;
    private static final int PROGRESS_X = GreenhouseGuiLayout.PROGRESS.x() - ORIGIN_X;
    private static final int PROGRESS_Y = GreenhouseGuiLayout.PROGRESS.y() - ORIGIN_Y;

    private static final ResourceLocation FLUID_OVERLAY = widget("fluid_slot");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable fluidOverlay;
    private final JeiProgressIndicator progressIndicator;

    public GreenhouseCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        icon = guiHelper.createDrawableItemStack(
                ModBlocks.GREENHOUSE.get().asItem().getDefaultInstance()
        );
        fluidOverlay = guiHelper.drawableBuilder(FLUID_OVERLAY, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
        progressIndicator = JeiProgressIndicator.create(
                guiHelper,
                GreenhouseGuiLayout.PROGRESS,
                40
        );
    }

    @Override
    public RecipeType<GreenhouseRecipe> getRecipeType() {
        return PoiesisJeiRecipeTypes.GREENHOUSE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.poiesis.category.greenhouse");
    }

    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            GreenhouseRecipe recipe,
            IFocusGroup focuses
    ) {
        // The compact JEI view uses one normal input slot for planting material;
        // all accepted planting ingredients are cycled by JEI in that slot.
        var plantingInput = builder.addInputSlot(INPUT_X, INPUT_Y)
                .setStandardSlotBackground()
                .setSlotName("planting_input");
        recipe.inputs().forEach(input ->
                plantingInput.addItemStacks(JeiStacks.displayStacks(input)));

        builder.addInputSlot(NUTRIENT_ITEM_X, NUTRIENT_ITEM_Y)
                .setStandardSlotBackground()
                .setSlotName("nutrient_item")
                .addItemStacks(JeiStacks.displayStacks(recipe.nutrient()));

        FluidStack water = new FluidStack(Fluids.WATER, recipe.waterAmount());
        builder.addSlot(RecipeIngredientRole.INPUT, WATER_X, WATER_Y)
                .setStandardSlotBackground()
                .setFluidRenderer(recipe.waterAmount(), false, 16, 16)
                .setOverlay(fluidOverlay, 0, 0)
                .setSlotName("water_input")
                .addFluidStack(water.getFluid(), water.getAmount());

        var nutrientFluid = builder.addSlot(
                        RecipeIngredientRole.INPUT,
                        NUTRIENT_FLUID_X,
                        NUTRIENT_FLUID_Y
                )
                .setStandardSlotBackground()
                .setFluidRenderer(recipe.nutrientFluid().amount(), false, 16, 16)
                .setOverlay(fluidOverlay, 0, 0)
                .setSlotName("nutrient_fluid_input");
        recipe.nutrientFluid().displayStacks().forEach(stack ->
                nutrientFluid.addFluidStack(stack.getFluid(), stack.getAmount(), stack.getTag())
        );

        List<ItemStack> results = recipe.results();
        if (!results.isEmpty()) {
            builder.addOutputSlot(FIRST_OUTPUT_X, FIRST_OUTPUT_Y)
                    .setStandardSlotBackground()
                    .setSlotName("item_output_0")
                    .addItemStack(results.get(0));
        }
        if (results.size() > 1) {
            builder.addOutputSlot(SECOND_OUTPUT_X, SECOND_OUTPUT_Y)
                    .setStandardSlotBackground()
                    .setSlotName("item_output_1")
                    .addItemStack(results.get(1));
        }

        FluidStack output = recipe.fluidOutput();
        builder.addSlot(RecipeIngredientRole.OUTPUT, FLUID_OUTPUT_X, FLUID_OUTPUT_Y)
                .setStandardSlotBackground()
                .setFluidRenderer(output.getAmount(), false, 16, 16)
                .setOverlay(fluidOverlay, 0, 0)
                .setSlotName("fluid_output")
                .addFluidStack(output.getFluid(), output.getAmount(), output.getTag());
    }

    @Override
    public void draw(
            GreenhouseRecipe recipe,
            IRecipeSlotsView slots,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        progressIndicator.draw(graphics, PROGRESS_X, PROGRESS_Y);
    }

    @Override
    public List<Component> getTooltipStrings(
            GreenhouseRecipe recipe,
            IRecipeSlotsView slots,
            double mouseX,
            double mouseY
    ) {
        if (!progressIndicator.contains(mouseX, mouseY, PROGRESS_X, PROGRESS_Y)) {
            return List.of();
        }
        return List.of(
                Component.translatable("jei.poiesis.recipe.duration", recipe.duration()),
                Component.translatable("jei.poiesis.recipe.base_power", recipe.baseEnergyPerTick())
        );
    }

    private static ResourceLocation widget(String name) {
        return new ResourceLocation(
                Poiesis.MOD_ID,
                "textures/container/widget/" + name + ".png"
        );
    }
}
