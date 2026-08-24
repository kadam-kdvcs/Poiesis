package org.kdvcs.poiesis.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.jei.ModuleFabricationJeiMode;
import org.kdvcs.poiesis.compat.jei.util.JeiStacks;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;

import java.util.List;

/** Compact JEI layout shared by both module substrate families. */
public final class ModuleFabricationCategory
        implements IRecipeCategory<ModuleFabricationRecipe> {

    private static final int WIDTH = 124;
    private static final int HEIGHT = 64;

    private static final int MATERIAL_GRID_X = 4;
    private static final int MATERIAL_GRID_Y = 5;
    private static final int SLOT_SPACING = 18;
    private static final int MATERIAL_COLUMNS = 3;
    private static final int MATERIAL_SLOT_COUNT = 9;

    private static final int TURN_X = 69;
    private static final int TURN_Y = 28;
    private static final int TURN_WIDTH = 20;
    private static final int TURN_HEIGHT = 16;

    // The substrate is centered directly above the process turn.
    private static final int MODULE_BASE_X =
            TURN_X + (TURN_WIDTH - SLOT_SPACING) / 2;
    private static final int MODULE_BASE_Y = 5;

    // The output slot and turn share the same vertical center.
    private static final int OUTPUT_X = 102;
    private static final int OUTPUT_Y =
            TURN_Y + (TURN_HEIGHT - SLOT_SPACING) / 2;

    private static final ResourceLocation TURN_TEXTURE =
            new ResourceLocation(
                    Poiesis.MOD_ID,
                    "textures/container/widget/turn.png"
            );

    private final ModuleFabricationJeiMode mode;
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable turn;

    public ModuleFabricationCategory(
            IGuiHelper guiHelper,
            ModuleFabricationJeiMode mode
    ) {
        this.mode = mode;
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(mode.substrateStack());
        this.turn = guiHelper.drawableBuilder(
                        TURN_TEXTURE,
                        0,
                        0,
                        TURN_WIDTH,
                        TURN_HEIGHT
                )
                .setTextureSize(TURN_WIDTH, TURN_HEIGHT)
                .build();
    }

    @Override
    public RecipeType<ModuleFabricationRecipe> getRecipeType() {
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
            ModuleFabricationRecipe recipe,
            IFocusGroup focuses
    ) {
        for (int index = 0; index < MATERIAL_SLOT_COUNT; index++) {
            int x = MATERIAL_GRID_X
                    + index % MATERIAL_COLUMNS * SLOT_SPACING;
            int y = MATERIAL_GRID_Y
                    + index / MATERIAL_COLUMNS * SLOT_SPACING;

            var slot = builder.addInputSlot(x, y)
                    .setStandardSlotBackground()
                    .setSlotName("material_" + (index + 1));

            if (index < recipe.materials().size()) {
                slot.addItemStacks(
                        JeiStacks.displayStacks(
                                recipe.materials().get(index)
                        )
                );
            }
        }

        builder.addInputSlot(MODULE_BASE_X, MODULE_BASE_Y)
                .setStandardSlotBackground()
                .setSlotName("module_base")
                .addItemStacks(
                        JeiStacks.displayStacks(recipe.moduleBase())
                );

        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y)
                .setStandardSlotBackground()
                .setSlotName("output")
                .addItemStack(recipe.result());
    }

    @Override
    public void draw(
            ModuleFabricationRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        turn.draw(graphics, TURN_X, TURN_Y);
    }

    @Override
    public List<Component> getTooltipStrings(
            ModuleFabricationRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            double mouseX,
            double mouseY
    ) {
        if (mouseX < TURN_X
                || mouseX >= TURN_X + TURN_WIDTH
                || mouseY < TURN_Y
                || mouseY >= TURN_Y + TURN_HEIGHT) {
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
