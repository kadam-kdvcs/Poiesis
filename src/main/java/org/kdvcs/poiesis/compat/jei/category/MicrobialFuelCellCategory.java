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
import net.minecraftforge.fluids.FluidStack;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.compat.jei.util.JeiProgressIndicator;
import org.kdvcs.poiesis.recipe.FluidIngredient;
import org.kdvcs.poiesis.recipe.MicrobialFuelCellRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.screen.layout.MicrobialFuelCellGuiLayout;

import java.util.List;

/** Ordered anolyte/catholyte view matching the MFC's two chamber layout. */
public final class MicrobialFuelCellCategory
        implements IRecipeCategory<MicrobialFuelCellRecipe> {
    private static final int ORIGIN_X = 39;
    private static final int ORIGIN_Y = 10;
    private static final int WIDTH = 102;
    private static final int HEIGHT = 74;

    private static final int ANOLYTE_X = MicrobialFuelCellGuiLayout.ANOLYTE_X - ORIGIN_X;
    private static final int ANOLYTE_Y = MicrobialFuelCellGuiLayout.ANOLYTE_Y - ORIGIN_Y;
    private static final int CATHOLYTE_X = MicrobialFuelCellGuiLayout.CATHOLYTE_X - ORIGIN_X;
    private static final int CATHOLYTE_Y = MicrobialFuelCellGuiLayout.CATHOLYTE_Y - ORIGIN_Y;
    private static final int PROGRESS_X = MicrobialFuelCellGuiLayout.PROGRESS.x() - ORIGIN_X;
    private static final int PROGRESS_Y = MicrobialFuelCellGuiLayout.PROGRESS.y() - ORIGIN_Y;
    private static final int FRAME_X = 2;
    private static final int FRAME_Y = 14;
    private static final int FRAME_WIDTH = 98;
    private static final int FRAME_HEIGHT = 58;

    private static final ResourceLocation FRAME_OVERLAY = widget("mfc_frame_overlay");
    private static final ResourceLocation TANK_OVERLAY = widget("mfc_tank_overlay");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable anolyteOverlay;
    private final IDrawable catholyteOverlay;
    private final JeiProgressIndicator progressIndicator;

    public MicrobialFuelCellCategory(IGuiHelper guiHelper) {
        background = new IDrawable() {
            @Override
            public int getWidth() {
                return WIDTH;
            }

            @Override
            public int getHeight() {
                return HEIGHT;
            }

            @Override
            public void draw(GuiGraphics graphics, int x, int y) {
                graphics.blit(
                        FRAME_OVERLAY,
                        x + FRAME_X,
                        y + FRAME_Y,
                        0,
                        0,
                        FRAME_WIDTH,
                        FRAME_HEIGHT,
                        FRAME_WIDTH,
                        FRAME_HEIGHT
                );
            }
        };
        icon = guiHelper.createDrawableItemStack(
                ModBlocks.MICROBIAL_FUEL_CELL.get().asItem().getDefaultInstance()
        );
        anolyteOverlay = guiHelper.drawableBuilder(
                        TANK_OVERLAY,
                        0,
                        0,
                        MicrobialFuelCellGuiLayout.ANOLYTE_WIDTH,
                        MicrobialFuelCellGuiLayout.ANOLYTE_HEIGHT
                )
                .setTextureSize(44, 70)
                .build();
        catholyteOverlay = guiHelper.drawableBuilder(
                        TANK_OVERLAY,
                        0,
                        35,
                        MicrobialFuelCellGuiLayout.CATHOLYTE_WIDTH,
                        MicrobialFuelCellGuiLayout.CATHOLYTE_HEIGHT
                )
                .setTextureSize(44, 70)
                .build();
        progressIndicator = JeiProgressIndicator.create(
                guiHelper,
                MicrobialFuelCellGuiLayout.PROGRESS,
                80
        );
    }

    @Override
    public RecipeType<MicrobialFuelCellRecipe> getRecipeType() {
        return PoiesisJeiRecipeTypes.MICROBIAL_FUEL_CELL;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.poiesis.category.microbial_fuel_cell");
    }

    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            MicrobialFuelCellRecipe recipe,
            IFocusGroup focuses
    ) {
        addChamber(
                builder,
                recipe.anolyte(),
                ANOLYTE_X,
                ANOLYTE_Y,
                MicrobialFuelCellGuiLayout.ANOLYTE_WIDTH,
                MicrobialFuelCellGuiLayout.ANOLYTE_HEIGHT,
                anolyteOverlay,
                "anolyte",
                "gui.poiesis.mfc.anolyte_chamber"
        );
        addChamber(
                builder,
                recipe.catholyte(),
                CATHOLYTE_X,
                CATHOLYTE_Y,
                MicrobialFuelCellGuiLayout.CATHOLYTE_WIDTH,
                MicrobialFuelCellGuiLayout.CATHOLYTE_HEIGHT,
                catholyteOverlay,
                "catholyte",
                "gui.poiesis.mfc.catholyte_chamber"
        );
    }

    private static void addChamber(
            IRecipeLayoutBuilder builder,
            FluidIngredient ingredient,
            int x,
            int y,
            int width,
            int height,
            IDrawable overlay,
            String slotName,
            String titleKey
    ) {
        IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                .setFluidRenderer(ingredient.amount(), false, width, height)
                .setOverlay(overlay, 0, 0)
                .setSlotName(slotName)
                .addTooltipCallback((slotView, tooltip) ->
                        tooltip.add(
                                0,
                                Component.translatable(titleKey)
                                        .withStyle(ChatFormatting.AQUA)
                        )
                );

        for (FluidStack stack : ingredient.displayStacks()) {
            slot.addFluidStack(
                    stack.getFluid(),
                    stack.getAmount(),
                    stack.getTag()
            );
        }
    }

    @Override
    public void draw(
            MicrobialFuelCellRecipe recipe,
            IRecipeSlotsView slots,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        progressIndicator.draw(graphics, PROGRESS_X, PROGRESS_Y);
    }

    @Override
    public List<Component> getTooltipStrings(
            MicrobialFuelCellRecipe recipe,
            IRecipeSlotsView slots,
            double mouseX,
            double mouseY
    ) {
        if (!progressIndicator.contains(mouseX, mouseY, PROGRESS_X, PROGRESS_Y)) {
            return List.of();
        }
        return List.of(
                Component.translatable(
                        "jei.poiesis.recipe.total_energy",
                        recipe.totalEnergy()
                ),
                Component.translatable(
                        "jei.poiesis.recipe.generation_rate",
                        recipe.energyPerTick()
                )
        );
    }

    private static ResourceLocation widget(String name) {
        return new ResourceLocation(
                Poiesis.MOD_ID,
                "textures/container/widget/" + name + ".png"
        );
    }
}
