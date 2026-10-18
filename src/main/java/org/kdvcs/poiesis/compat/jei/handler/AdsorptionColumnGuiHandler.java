package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.screen.AdsorptionColumnScreen;
import org.kdvcs.poiesis.screen.layout.AdsorptionColumnGuiLayout;
import org.kdvcs.poiesis.compat.jei.util.JeiTankClickables;
import org.kdvcs.poiesis.compat.jei.util.TankSpec;
import mezz.jei.api.runtime.IClickableIngredient;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class AdsorptionColumnGuiHandler
        implements IGuiContainerHandler<AdsorptionColumnScreen> {
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            AdsorptionColumnScreen screen,
            double guiMouseX,
            double guiMouseY
    ) {
        return List.of(
                IGuiClickableArea.createBasic(
                        AdsorptionColumnGuiLayout.PROGRESS_X,
                        AdsorptionColumnGuiLayout.PROGRESS_Y,
                        AdsorptionColumnGuiLayout.PROGRESS_WIDTH,
                        AdsorptionColumnGuiLayout.PROGRESS_HEIGHT,
                        PoiesisJeiRecipeTypes.ADSORPTION
                )
        );
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
            AdsorptionColumnScreen screen, double mouseX, double mouseY) {
        return JeiTankClickables.findTankIngredientUnderMouse(
                screen, screen.getGuiLeft(), screen.getGuiTop(), mouseX, mouseY,
                List.of(
                        new TankSpec(AdsorptionColumnGuiLayout.FLUID_INPUT_X,
                                AdsorptionColumnGuiLayout.FLUID_INPUT_Y,
                                AdsorptionColumnGuiLayout.FLUID_SIZE,
                                AdsorptionColumnGuiLayout.FLUID_SIZE,
                                value -> ((AdsorptionColumnScreen) value).getMenu().inputFluidStack()),
                        new TankSpec(AdsorptionColumnGuiLayout.FLUID_OUTPUT_X,
                                AdsorptionColumnGuiLayout.FLUID_OUTPUT_Y,
                                AdsorptionColumnGuiLayout.FLUID_SIZE,
                                AdsorptionColumnGuiLayout.FLUID_SIZE,
                                value -> ((AdsorptionColumnScreen) value).getMenu().outputFluidStack())
                )
        );
    }
}
