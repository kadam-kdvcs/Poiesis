package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.compat.jei.util.JeiTankClickables;
import org.kdvcs.poiesis.compat.jei.util.TankSpec;
import org.kdvcs.poiesis.screen.GasGeneratorScreen;
import org.kdvcs.poiesis.screen.layout.GasGeneratorGuiLayout;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Lets JEI inspect the fuel currently stored in the generator GUI. */
public final class GasGeneratorGuiHandler
        implements IGuiContainerHandler<GasGeneratorScreen> {
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            GasGeneratorScreen screen,
            double guiMouseX,
            double guiMouseY
    ) {
        return List.of(IGuiClickableArea.createBasic(
                GasGeneratorGuiLayout.FIRE_X,
                GasGeneratorGuiLayout.FIRE_Y,
                GasGeneratorGuiLayout.FIRE_SIZE,
                GasGeneratorGuiLayout.FIRE_SIZE,
                PoiesisJeiRecipeTypes.GAS_GENERATION
        ));
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
            GasGeneratorScreen screen,
            double mouseX,
            double mouseY
    ) {
        return JeiTankClickables.findTankIngredientUnderMouse(
                screen,
                screen.getGuiLeft(),
                screen.getGuiTop(),
                mouseX,
                mouseY,
                List.of(new TankSpec(
                        GasGeneratorGuiLayout.FLUID_X,
                        GasGeneratorGuiLayout.FLUID_Y,
                        GasGeneratorGuiLayout.FLUID_SIZE,
                        GasGeneratorGuiLayout.FLUID_SIZE,
                        value -> ((GasGeneratorScreen) value).getMenu().fluidStack()
                ))
        );
    }
}
