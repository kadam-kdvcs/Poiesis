package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.screen.BiomassGasifierScreen;
import org.kdvcs.poiesis.screen.layout.BiomassGasifierGuiLayout;

import java.util.Collection;
import java.util.List;

/** Opens the gasification page from the flame in the machine GUI. */
public final class BiomassGasifierGuiHandler
        implements IGuiContainerHandler<BiomassGasifierScreen> {
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            BiomassGasifierScreen screen,
            double guiMouseX,
            double guiMouseY
    ) {
        return List.of(IGuiClickableArea.createBasic(
                BiomassGasifierGuiLayout.FIRE_X,
                BiomassGasifierGuiLayout.FIRE_Y,
                BiomassGasifierGuiLayout.FIRE_SIZE,
                BiomassGasifierGuiLayout.FIRE_SIZE,
                PoiesisJeiRecipeTypes.GASIFICATION
        ));
    }
}
