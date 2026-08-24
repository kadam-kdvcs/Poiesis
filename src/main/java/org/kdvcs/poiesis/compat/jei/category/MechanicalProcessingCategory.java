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
import org.kdvcs.poiesis.compat.jei.MechanicalProcessingJeiMode;
import org.kdvcs.poiesis.compat.jei.util.JeiProgressIndicator;
import org.kdvcs.poiesis.compat.jei.util.JeiStacks;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipe;
import org.kdvcs.poiesis.screen.layout.MechanicalProcessorGuiLayout;
import org.kdvcs.poiesis.screen.layout.ProgressIndicatorLayout;

import java.util.List;

/**
 * One reusable category implementation backs all Mechanical Processor modes.
 *
 * The process indicator is layered exactly like the machine GUI:
 *  - a static mode-specific base
 *  - a looping foreground progress arrow
 */
public final class MechanicalProcessingCategory
        implements IRecipeCategory<MechanicalProcessingRecipe> {

    private static final int WIDTH = 128;
    private static final int HEIGHT = 58;

    private static final int INPUT_X = 12;
    private static final int INPUT_Y = 13;

    private static final int OUTPUT_X = 98;
    private static final int OUTPUT_Y = 13;

    private static final int ARROW_X = 53;
    private static final int ARROW_CENTER_Y = 21;

    private static final int MODULE_X = 55;
    private static final int MODULE_Y = 38;

    /**
     * JEI's process arrow is demonstrative, not tied to a running machine.
     * 40 ticks gives a clear two-second left-to-right loop.
     */
    private static final int JEI_ARROW_CYCLE_TICKS = 40;

    private final MechanicalProcessingJeiMode mode;
    private final IDrawable background;
    private final IDrawable icon;

    private final JeiProgressIndicator progressIndicator;

    private final int progressX;
    private final int progressY;
    private final int progressWidth;
    private final int progressHeight;

    public MechanicalProcessingCategory(
            IGuiHelper guiHelper,
            MechanicalProcessingJeiMode mode
    ) {
        this.mode = mode;
        this.background = guiHelper.createBlankDrawable(
                WIDTH,
                HEIGHT
        );
        this.icon = guiHelper.createDrawableItemStack(
                mode.moduleStack()
        );

        ProgressIndicatorLayout visual =
                MechanicalProcessorGuiLayout.progressVisual(
                        mode.machineMode()
                );

        if (visual == null) {
            throw new IllegalArgumentException(
                    "JEI processor category requires a real processing mode"
            );
        }

        this.progressIndicator =
                JeiProgressIndicator.create(
                        guiHelper,
                        visual,
                        JEI_ARROW_CYCLE_TICKS
                );

        this.progressWidth = progressIndicator.width();
        this.progressHeight = progressIndicator.height();

        this.progressX = ARROW_X;
        this.progressY =
                ARROW_CENTER_Y - (progressHeight - 1) / 2;
    }

    @Override
    public RecipeType<MechanicalProcessingRecipe> getRecipeType() {
        return mode.recipeType();
    }

    @Override
    public Component getTitle() {
        return mode.title();
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
            MechanicalProcessingRecipe recipe,
            IFocusGroup focuses
    ) {
        builder.addInputSlot(
                        INPUT_X,
                        INPUT_Y
                )
                .setStandardSlotBackground()
                .setSlotName("input")
                .addItemStacks(
                        JeiStacks.displayStacks(
                                recipe.input()
                        )
                );

        builder.addOutputSlot(
                        OUTPUT_X,
                        OUTPUT_Y
                )
                .setOutputSlotBackground()
                .setSlotName("output")
                .addItemStack(
                        recipe.result()
                );

        builder.addSlot(
                        RecipeIngredientRole.CATALYST,
                        MODULE_X,
                        MODULE_Y
                )
                .setStandardSlotBackground()
                .setSlotName("specialization_module")
                .addItemStack(
                        mode.moduleStack()
                );
    }

    @Override
    public void draw(
            MechanicalProcessingRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        progressIndicator.draw(
                graphics,
                progressX,
                progressY
        );
    }

    @Override
    public List<Component> getTooltipStrings(
            MechanicalProcessingRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY
    ) {
        if (!isOverProgressArrow(mouseX, mouseY)) {
            return List.of();
        }

        return List.of(
                Component.translatable(
                        "jei.poiesis.recipe.duration",
                        recipe.duration()
                ),
                Component.translatable(
                        "jei.poiesis.recipe.base_power",
                        recipe.baseEnergyPerTick()
                )
        );
    }

    private boolean isOverProgressArrow(
            double mouseX,
            double mouseY
    ) {
        return progressIndicator.contains(
                mouseX,
                mouseY,
                progressX,
                progressY
        );
    }
}
