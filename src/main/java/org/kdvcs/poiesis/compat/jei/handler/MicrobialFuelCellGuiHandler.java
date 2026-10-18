package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.compat.jei.util.JeiTankClickables;
import org.kdvcs.poiesis.compat.jei.util.TankSpec;
import org.kdvcs.poiesis.screen.MicrobialFuelCellScreen;
import org.kdvcs.poiesis.screen.layout.MicrobialFuelCellGuiLayout;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Exposes the ordered MFC reaction and both fluid chambers to JEI. */
public final class MicrobialFuelCellGuiHandler
        implements IGuiContainerHandler<MicrobialFuelCellScreen> {
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            MicrobialFuelCellScreen screen,
            double guiMouseX,
            double guiMouseY
    ) {
        var progress = MicrobialFuelCellGuiLayout.PROGRESS;
        return List.of(IGuiClickableArea.createBasic(
                progress.x(),
                progress.y(),
                progress.width(),
                progress.height(),
                PoiesisJeiRecipeTypes.MICROBIAL_FUEL_CELL
        ));
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
            MicrobialFuelCellScreen screen,
            double mouseX,
            double mouseY
    ) {
        return JeiTankClickables.findTankIngredientUnderMouse(
                screen,
                screen.getGuiLeft(),
                screen.getGuiTop(),
                mouseX,
                mouseY,
                List.of(
                        new TankSpec(
                                MicrobialFuelCellGuiLayout.ANOLYTE_X,
                                MicrobialFuelCellGuiLayout.ANOLYTE_Y,
                                MicrobialFuelCellGuiLayout.ANOLYTE_WIDTH,
                                MicrobialFuelCellGuiLayout.ANOLYTE_HEIGHT,
                                value -> ((MicrobialFuelCellScreen) value)
                                        .getMenu().anolyteStack()
                        ),
                        new TankSpec(
                                MicrobialFuelCellGuiLayout.CATHOLYTE_X,
                                MicrobialFuelCellGuiLayout.CATHOLYTE_Y,
                                MicrobialFuelCellGuiLayout.CATHOLYTE_WIDTH,
                                MicrobialFuelCellGuiLayout.CATHOLYTE_HEIGHT,
                                value -> ((MicrobialFuelCellScreen) value)
                                        .getMenu().catholyteStack()
                        )
                )
        );
    }
}
