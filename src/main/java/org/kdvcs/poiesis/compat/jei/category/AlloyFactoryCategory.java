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
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.compat.jei.util.JeiStacks;
import org.kdvcs.poiesis.recipe.alloy.AlloyTableRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;

import java.util.List;

/** JEI page for one Alloy Factory recipe and all outputs in its result tag. */
public final class AlloyFactoryCategory
        implements IRecipeCategory<AlloyTableRecipe> {

    private static final int WIDTH = 162;
    private static final int HEIGHT = 62;

    private static final int MAX_INPUTS = 18;
    private static final int INPUT_COLUMNS = 6;
    private static final int INPUT_GRID_X = 4;
    private static final int INPUT_GRID_WIDTH = 108;
    private static final int SLOT_SPACING = 18;

    private static final int PROGRESS_X = 124;
    private static final int PROGRESS_Y = 23;
    private static final int PROGRESS_BASE_WIDTH = 6;
    private static final int PROGRESS_BASE_HEIGHT = 16;
    private static final int PROGRESS_WIDTH = 4;
    private static final int PROGRESS_HEIGHT = 14;
    private static final int OUTPUT_X = 140;
    private static final int OUTPUT_Y = 22;
    private static final int JEI_PROGRESS_CYCLE_TICKS = 40;

    private static final ResourceLocation PROGRESS_BASE_TEXTURE =
            new ResourceLocation(
                    Poiesis.MOD_ID,
                    "textures/container/widget/alloy_factory_base.png"
            );

    private static final ResourceLocation PROGRESS_TEXTURE =
            new ResourceLocation(
                    Poiesis.MOD_ID,
                    "textures/container/widget/alloy_factory_progress_arrow.png"
            );

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableStatic progressBase;
    private final IDrawableAnimated progress;

    public AlloyFactoryCategory(IGuiHelper guiHelper) {
        background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        icon = guiHelper.createDrawableItemStack(
                ModBlocks.ALLOY_FACTORY.get().asItem().getDefaultInstance()
        );
        progressBase = guiHelper.drawableBuilder(
                        PROGRESS_BASE_TEXTURE,
                        0,
                        0,
                        PROGRESS_BASE_WIDTH,
                        PROGRESS_BASE_HEIGHT
                )
                .setTextureSize(PROGRESS_BASE_WIDTH, PROGRESS_BASE_HEIGHT)
                .build();
        IDrawableStatic progressSprite = guiHelper.drawableBuilder(
                        PROGRESS_TEXTURE,
                        0,
                        0,
                        PROGRESS_WIDTH,
                        PROGRESS_HEIGHT
                )
                .setTextureSize(PROGRESS_WIDTH, PROGRESS_HEIGHT)
                .build();
        progress = guiHelper.createAnimatedDrawable(
                progressSprite,
                JEI_PROGRESS_CYCLE_TICKS,
                IDrawableAnimated.StartDirection.BOTTOM,
                false
        );
    }

    @Override
    public RecipeType<AlloyTableRecipe> getRecipeType() {
        return PoiesisJeiRecipeTypes.ALLOYING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.poiesis.category.alloy_factory");
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
            AlloyTableRecipe recipe,
            IFocusGroup focuses
    ) {
        int inputCount = Math.min(recipe.inputs().size(), MAX_INPUTS);
        int rows = Math.max(1, (inputCount + INPUT_COLUMNS - 1) / INPUT_COLUMNS);
        int firstY = (HEIGHT - rows * SLOT_SPACING) / 2;

        for (int index = 0; index < inputCount; index++) {
            int row = index / INPUT_COLUMNS;
            int indexInRow = index % INPUT_COLUMNS;
            int remaining = inputCount - row * INPUT_COLUMNS;
            int columnsInRow = Math.min(INPUT_COLUMNS, remaining);
            int rowWidth = columnsInRow * SLOT_SPACING;
            int firstX = INPUT_GRID_X + (INPUT_GRID_WIDTH - rowWidth) / 2;

            builder.addInputSlot(
                            firstX + indexInRow * SLOT_SPACING,
                            firstY + row * SLOT_SPACING
                    )
                    .setStandardSlotBackground()
                    .setSlotName("material_" + (index + 1))
                    .addItemStacks(JeiStacks.displayStacks(recipe.inputs().get(index)));
        }

        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y)
                .setOutputSlotBackground()
                .setSlotName("output")
                .addItemStacks(JeiStacks.tagStacks(
                        recipe.resultTag(),
                        recipe.resultCount()
                ));
    }

    @Override
    public void draw(
            AlloyTableRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        progressBase.draw(graphics, PROGRESS_X, PROGRESS_Y);
        progress.draw(graphics, PROGRESS_X + 1, PROGRESS_Y + 1);
    }

    @Override
    public List<Component> getTooltipStrings(
            AlloyTableRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY
    ) {
        if (mouseX < PROGRESS_X
                || mouseX >= PROGRESS_X + PROGRESS_BASE_WIDTH
                || mouseY < PROGRESS_Y
                || mouseY >= PROGRESS_Y + PROGRESS_BASE_HEIGHT) {
            return List.of();
        }

        return List.of(
                Component.translatable(
                        "jei.poiesis.recipe.duration",
                        recipe.duration()
                ),
                Component.translatable(
                        "jei.poiesis.recipe.base_power",
                        recipe.energyPerTick()
                )
        );
    }
}
