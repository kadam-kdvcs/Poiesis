package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.screen.SeparatorScreen;
import org.kdvcs.poiesis.screen.layout.ProgressIndicatorLayout;
import org.kdvcs.poiesis.screen.layout.SeparatorGuiLayout;

import java.util.Collection;
import java.util.List;

/** Makes the Separator's processing indicator open both of its JEI pages. */
public final class SeparatorGuiHandler
        implements IGuiContainerHandler<SeparatorScreen> {

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            SeparatorScreen screen,
            double guiMouseX,
            double guiMouseY
    ) {
        ProgressIndicatorLayout visual = SeparatorGuiLayout.PROGRESS;

        return List.of(
                IGuiClickableArea.createBasic(
                        visual.x(),
                        visual.y(),
                        visual.width(),
                        visual.height(),
                        PoiesisJeiRecipeTypes.DRY_SEPARATING,
                        PoiesisJeiRecipeTypes.WET_SEPARATING
                )
        );
    }
}
