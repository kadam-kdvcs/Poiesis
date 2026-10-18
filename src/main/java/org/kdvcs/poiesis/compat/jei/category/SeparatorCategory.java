package org.kdvcs.poiesis.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.jei.SeparatorJeiMode;
import org.kdvcs.poiesis.compat.jei.util.JeiProgressIndicator;
import org.kdvcs.poiesis.compat.jei.util.JeiStacks;
import org.kdvcs.poiesis.recipe.ChanceOutput;
import org.kdvcs.poiesis.recipe.CountedIngredient;
import org.kdvcs.poiesis.recipe.FluidIngredient;
import org.kdvcs.poiesis.recipe.SeparatorRecipe;
import org.kdvcs.poiesis.registry.ModFluids;
import org.kdvcs.poiesis.screen.layout.SeparatorGuiLayout;
import org.kdvcs.poiesis.tag.ModTags;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** Shared JEI layout for all strictly isolated Separator process modes. */
public final class SeparatorCategory
        implements IRecipeCategory<SeparatorRecipe> {

    private static final int WIDTH = SeparatorGuiLayout.JEI_WIDTH;
    private static final int HEIGHT = SeparatorGuiLayout.JEI_HEIGHT + 2;

    private static final int FLUID_X = SeparatorGuiLayout.jeiX(
            SeparatorGuiLayout.FLUID_X
    );
    private static final int FLUID_Y = SeparatorGuiLayout.jeiY(
            SeparatorGuiLayout.FLUID_Y
    );

    private static final int INPUT_X = SeparatorGuiLayout.jeiX(
            SeparatorGuiLayout.FIRST_INPUT_X
    );
    private static final int FIRST_INPUT_Y = SeparatorGuiLayout.jeiY(
            SeparatorGuiLayout.FIRST_INPUT_Y
    );
    private static final int SECOND_INPUT_Y = SeparatorGuiLayout.jeiY(
            SeparatorGuiLayout.SECOND_INPUT_Y
    );

    private static final int[] OUTPUT_X = {
            SeparatorGuiLayout.jeiX(SeparatorGuiLayout.OUTPUT_X[0]),
            SeparatorGuiLayout.jeiX(SeparatorGuiLayout.OUTPUT_X[1]),
            SeparatorGuiLayout.jeiX(SeparatorGuiLayout.OUTPUT_X[2]),
            SeparatorGuiLayout.jeiX(SeparatorGuiLayout.OUTPUT_X[3])
    };
    private static final int[] OUTPUT_Y = {
            SeparatorGuiLayout.jeiY(SeparatorGuiLayout.OUTPUT_Y[0]),
            SeparatorGuiLayout.jeiY(SeparatorGuiLayout.OUTPUT_Y[1]),
            SeparatorGuiLayout.jeiY(SeparatorGuiLayout.OUTPUT_Y[2]),
            SeparatorGuiLayout.jeiY(SeparatorGuiLayout.OUTPUT_Y[3])
    };

    private static final int ARROW_X = SeparatorGuiLayout.jeiX(
            SeparatorGuiLayout.PROGRESS.x()
    );
    private static final int ARROW_Y = SeparatorGuiLayout.jeiY(
            SeparatorGuiLayout.PROGRESS.y()
    );

    private static final int MODULE_X = ARROW_X + 1;
    private static final int MODULE_Y =
            ARROW_Y + SeparatorGuiLayout.PROGRESS.height() + 2;

    private static final int JEI_ARROW_CYCLE_TICKS = 40;

    private static final ResourceLocation FLUID_SLOT_OVERLAY =
            new ResourceLocation(
                    Poiesis.MOD_ID,
                    "textures/container/widget/fluid_slot.png"
            );

    private static final ResourceLocation ELECTRODE_SLOT =
            new ResourceLocation(
                    Poiesis.MOD_ID,
                    "textures/container/widget/electrode_slot.png"
            );

    private final SeparatorJeiMode mode;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable fluidSlotOverlay;
    private final IDrawable electrodeSlot;
    private final JeiProgressIndicator progressIndicator;

    public SeparatorCategory(
            IGuiHelper guiHelper,
            SeparatorJeiMode mode
    ) {
        this.mode = mode;
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(mode.iconStack());
        this.fluidSlotOverlay = guiHelper.drawableBuilder(
                        FLUID_SLOT_OVERLAY,
                        0,
                        0,
                        SeparatorGuiLayout.FLUID_SIZE,
                        SeparatorGuiLayout.FLUID_SIZE
                )
                .setTextureSize(
                        SeparatorGuiLayout.FLUID_SIZE,
                        SeparatorGuiLayout.FLUID_SIZE
                )
                .build();
        this.electrodeSlot = guiHelper.drawableBuilder(
                        ELECTRODE_SLOT,
                        0,
                        0,
                        SeparatorGuiLayout.SLOT_SIZE,
                        SeparatorGuiLayout.SLOT_SIZE
                )
                .setTextureSize(
                        SeparatorGuiLayout.SLOT_SIZE,
                        SeparatorGuiLayout.SLOT_SIZE
                )
                .build();
        this.progressIndicator = JeiProgressIndicator.create(
                guiHelper,
                SeparatorGuiLayout.PROGRESS,
                JEI_ARROW_CYCLE_TICKS
        );
    }

    @Override
    public RecipeType<SeparatorRecipe> getRecipeType() {
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
            SeparatorRecipe recipe,
            IFocusGroup focuses
    ) {
        addItemInputs(builder, recipe.inputs());
        addOutputs(builder, recipe);

        if (mode.wet()) {
            addFluidInput(builder, recipe.fluid());
        } else if (mode.electrostatic()) {
            builder.addSlot(
                            RecipeIngredientRole.CATALYST,
                            FLUID_X,
                            FLUID_Y
                    )
                    .setSlotName("electrode")
                    .addItemStacks(Arrays.asList(
                            Ingredient.of(ModTags.Items.ELECTRODES).getItems()
                    ));
        }

        if (!mode.requiredModuleStack().isEmpty()) {
            builder.addSlot(
                            RecipeIngredientRole.CATALYST,
                            MODULE_X,
                            MODULE_Y
                    )
                    .setStandardSlotBackground()
                    .setSlotName("specialization_module")
                    .addItemStack(mode.requiredModuleStack());
        }
    }

    private static void addItemInputs(
            IRecipeLayoutBuilder builder,
            List<CountedIngredient> inputs
    ) {
        if (!inputs.isEmpty()) {
            var slot = builder.addInputSlot(INPUT_X, FIRST_INPUT_Y)
                    .setStandardSlotBackground()
                    .setSlotName("input_1")
                    .addItemStacks(JeiStacks.displayStacks(inputs.get(0)));
            addNonConsumedTooltip(slot, inputs.get(0));
        }

        if (inputs.size() >= 2) {
            var slot = builder.addInputSlot(INPUT_X, SECOND_INPUT_Y)
                    .setStandardSlotBackground()
                    .setSlotName("input_2")
                    .addItemStacks(JeiStacks.displayStacks(inputs.get(1)));
            addNonConsumedTooltip(slot, inputs.get(1));
        }
    }

    private static void addNonConsumedTooltip(
            IRecipeSlotBuilder slot,
            CountedIngredient input
    ) {
        if (!input.consumes()) {
            slot.addTooltipCallback((slotView, tooltip) ->
                    tooltip.add(Component.translatable(
                            "jei.poiesis.recipe.input_not_consumed"
                    ).withStyle(ChatFormatting.RED))
            );
        }
    }

    private void addFluidInput(
            IRecipeLayoutBuilder builder,
            @Nullable FluidIngredient ingredient
    ) {
        if (ingredient == null) {
            return;
        }

        var slot = builder.addInputSlot(FLUID_X, FLUID_Y)
                .setStandardSlotBackground()
                .setFluidRenderer(ingredient.amount(), false, 16, 16)
                .setOverlay(fluidSlotOverlay, 0, 0)
                .setSlotName("fluid_input");

        for (FluidStack stack : ingredient.displayStacks()) {
            slot.addFluidStack(
                    stack.getFluid(),
                    stack.getAmount(),
                    stack.getTag()
            );
        }
    }

    private void addOutputs(
            IRecipeLayoutBuilder builder,
            SeparatorRecipe recipe
    ) {
        List<ChanceOutput> outputs = recipe.outputs();
        int itemOutputCount = mode.rinsing()
                ? Math.min(SeparatorRecipe.MAX_OUTPUTS - 1, outputs.size())
                : outputs.size();
        for (int index = 0; index < itemOutputCount; index++) {
            ChanceOutput output = outputs.get(index);

            var slot = builder.addOutputSlot(OUTPUT_X[index], OUTPUT_Y[index])
                    // JEI's emphasized output frame is wider than 18px and
                    // overlaps in the Separator's compact 2x2 output grid.
                    .setStandardSlotBackground()
                    .setSlotName("output_" + (index + 1))
                    .addItemStack(output.stack());

            if (output.chance() < 1.0D) {
                slot.addTooltipCallback((slotView, tooltip) ->
                        tooltip.add(Component.translatable(
                                "jei.poiesis.recipe.output_chance",
                                formatChance(output.chance())
                        ))
                );
            }
        }

        if (mode.rinsing() && recipe.fluid() != null) {
            int lastOutputIndex = Math.max(0, itemOutputCount - 1);
            builder.addOutputSlot(
                            OUTPUT_X[lastOutputIndex]
                                    + SeparatorGuiLayout.SLOT_SIZE,
                            OUTPUT_Y[lastOutputIndex]
                    )
                    .setStandardSlotBackground()
                    .setFluidRenderer(recipe.fluid().amount(), false, 16, 16)
                    .setOverlay(fluidSlotOverlay, 0, 0)
                    .setSlotName("wastewater_output")
                    .addFluidStack(
                            ModFluids.WASTEWATER.get(),
                            recipe.fluid().amount()
                    );
        }
    }

    private static String formatChance(double chance) {
        BigDecimal percentage = BigDecimal
                .valueOf(chance * 100.0D)
                .stripTrailingZeros();
        return percentage.toPlainString() + "%";
    }

    @Override
    public void draw(
            SeparatorRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        if (mode.electrostatic()) {
            electrodeSlot.draw(graphics, FLUID_X - 1, FLUID_Y - 1);
        }
        progressIndicator.draw(graphics, ARROW_X, ARROW_Y);
    }

    @Override
    public List<Component> getTooltipStrings(
            SeparatorRecipe recipe,
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
