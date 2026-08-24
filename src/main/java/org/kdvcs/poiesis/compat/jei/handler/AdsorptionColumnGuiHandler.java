package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.screen.AdsorptionColumnScreen;
import org.kdvcs.poiesis.screen.layout.AdsorptionColumnGuiLayout;

import java.util.Collection;
import java.util.List;

public final class AdsorptionColumnGuiHandler
        implements IGuiContainerHandler<AdsorptionColumnScreen> {
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            AdsorptionColumnScreen screen,
            double guiMouseX,
            double guiMouseY
    ) {
        return List.of(IGuiClickableArea.createBasic(
                AdsorptionColumnGuiLayout.PROGRESS_X,
                AdsorptionColumnGuiLayout.PROGRESS_Y,
                AdsorptionColumnGuiLayout.PROGRESS_WIDTH,
                AdsorptionColumnGuiLayout.PROGRESS_HEIGHT,
                PoiesisJeiRecipeTypes.ADSORPTION
        ));
    }
}
