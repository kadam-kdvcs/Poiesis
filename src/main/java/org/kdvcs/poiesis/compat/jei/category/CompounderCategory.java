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
import org.kdvcs.poiesis.compat.jei.CompounderJeiMode;
import org.kdvcs.poiesis.compat.jei.util.JeiProgressIndicator;
import org.kdvcs.poiesis.compat.jei.util.JeiStacks;
import org.kdvcs.poiesis.recipe.CompounderRecipe;
import org.kdvcs.poiesis.recipe.CountedIngredient;
import org.kdvcs.poiesis.screen.layout.CompounderGuiLayout;

import java.util.List;

/**
 * JEI page for the Compounder.
 *
 * The category mirrors the machine's real 2-input / 1-output process geometry.
 * Slot and arrow positions are normalized from CompounderGuiLayout, keeping the
 * JEI page compact while preserving the same visual relationship players see
 * in the actual GUI.
 *
 * Specialized pages place their required process module beneath the arrow,
 * matching the visual cue used by the Mechanical Processor's JEI pages.
 */
public final class CompounderCategory
        implements IRecipeCategory<CompounderRecipe> {

    /*
     * Keep the exact spatial relationship from the real Compounder GUI:
     *
     * input 1  (37,17)       arrow (60,26)      output (120,35)
     * input 2  (37,52)
     *
     * CompounderGuiLayout normalizes that 101x53 process cluster and adds
     * only a 4px outer margin, producing a compact JEI page.
     */
    private static final int WIDTH =
            CompounderGuiLayout.JEI_WIDTH;

    private static final int HEIGHT =
            CompounderGuiLayout.JEI_HEIGHT;

    private static final int INPUT_X =
            CompounderGuiLayout.jeiX(
                    CompounderGuiLayout.FIRST_INPUT_X
            );

    private static final int FIRST_INPUT_Y =
            CompounderGuiLayout.jeiY(
                    CompounderGuiLayout.FIRST_INPUT_Y
            );

    private static final int SECOND_INPUT_Y =
            CompounderGuiLayout.jeiY(
                    CompounderGuiLayout.SECOND_INPUT_Y
            );

    private static final int OUTPUT_X =
            CompounderGuiLayout.jeiX(
                    CompounderGuiLayout.OUTPUT_X
            );

    private static final int OUTPUT_Y =
            CompounderGuiLayout.jeiY(
                    CompounderGuiLayout.OUTPUT_Y
            );

    private static final int ARROW_X =
            CompounderGuiLayout.jeiX(
                    CompounderGuiLayout.STANDARD_PROGRESS.x()
            );

    private static final int ARROW_Y =
            CompounderGuiLayout.jeiY(
                    CompounderGuiLayout.STANDARD_PROGRESS.y()
            );

    private static final int MODULE_X =
            ARROW_X + (49 - 18) / 2;

    private static final int MODULE_Y =
            CompounderGuiLayout.JEI_HEIGHT + 1;

    private static final int JEI_ARROW_CYCLE_TICKS = 40;

    private final CompounderJeiMode mode;
    private final IDrawable background;
    private final IDrawable icon;
    private final JeiProgressIndicator progressIndicator;

    public CompounderCategory(
            IGuiHelper guiHelper,
            CompounderJeiMode mode
    ) {
        this.mode = mode;
        this.background =
                guiHelper.createBlankDrawable(
                        WIDTH,
                        mode.specialized()
                                ? HEIGHT + 20
                                : HEIGHT
                );

        this.icon =
                guiHelper.createDrawableItemStack(
                        mode.iconStack()
                );

        this.progressIndicator =
                JeiProgressIndicator.create(
                        guiHelper,
                        mode.progressVisual(),
                        JEI_ARROW_CYCLE_TICKS
                );
    }

    @Override
    public RecipeType<CompounderRecipe> getRecipeType() {
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
            CompounderRecipe recipe,
            IFocusGroup focuses
    ) {
        List<CountedIngredient> inputs =
                recipe.inputs();

        if (!inputs.isEmpty()) {
            builder.addInputSlot(
                            INPUT_X,
                            FIRST_INPUT_Y
                    )
                    .setStandardSlotBackground()
                    .setSlotName("input_1")
                    .addItemStacks(
                            JeiStacks.displayStacks(
                                    inputs.get(0)
                            )
                    );
        }

        if (inputs.size() >= 2) {
            builder.addInputSlot(
                            INPUT_X,
                            SECOND_INPUT_Y
                    )
                    .setStandardSlotBackground()
                    .setSlotName("input_2")
                    .addItemStacks(
                            JeiStacks.displayStacks(
                                    inputs.get(1)
                            )
                    );
        }

        builder.addOutputSlot(
                        OUTPUT_X,
                        OUTPUT_Y
                )
                .setOutputSlotBackground()
                .setSlotName("output")
                .addItemStack(
                        recipe.result()
                );

        if (mode.specialized()) {
            builder.addSlot(
                            RecipeIngredientRole.CATALYST,
                            MODULE_X,
                            MODULE_Y
                    )
                    .setStandardSlotBackground()
                    .setSlotName("high_shear_module")
                    .addItemStack(mode.moduleStack());
        }

    }

    @Override
    public void draw(
            CompounderRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        progressIndicator.draw(
                graphics,
                ARROW_X,
                ARROW_Y
        );
    }

    @Override
    public List<Component> getTooltipStrings(
            CompounderRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY
    ) {
        if (!progressIndicator.contains(
                mouseX,
                mouseY,
                ARROW_X,
                ARROW_Y
        )) {
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
}
