package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.screen.ModuleFabricatorScreen;

import java.util.Collection;
import java.util.List;

/** Opens both fabrication pages from the machine's real progress indicator. */
public final class ModuleFabricatorGuiHandler
        implements IGuiContainerHandler<ModuleFabricatorScreen> {

    private static final int PROGRESS_X = 43;
    private static final int PROGRESS_Y = 51;
    private static final int PROGRESS_WIDTH = 10;
    private static final int PROGRESS_HEIGHT = 19;

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            ModuleFabricatorScreen screen,
            double guiMouseX,
            double guiMouseY
    ) {
        return List.of(
                IGuiClickableArea.createBasic(
                        PROGRESS_X,
                        PROGRESS_Y,
                        PROGRESS_WIDTH,
                        PROGRESS_HEIGHT,
                        PoiesisJeiRecipeTypes.SPECIALIZATION_MODULE_FABRICATION,
                        PoiesisJeiRecipeTypes.PERFORMANCE_MODULE_FABRICATION
                )
        );
    }
}
