package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.kdvcs.poiesis.compat.jei.CompounderJeiMode;
import org.kdvcs.poiesis.screen.CompounderScreen;
import org.kdvcs.poiesis.screen.layout.CompounderGuiLayout;
import org.kdvcs.poiesis.screen.layout.ProgressIndicatorLayout;

import java.util.Collection;
import java.util.List;

/**
 * Makes the Compounder's real processing arrow open its JEI recipe page.
 */
public final class CompounderGuiHandler
        implements IGuiContainerHandler<CompounderScreen> {

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            CompounderScreen screen,
            double guiMouseX,
            double guiMouseY
    ) {
        ProgressIndicatorLayout visual =
                CompounderGuiLayout.progressVisual(
                        screen.highShearEnabled()
                );

        CompounderJeiMode mode =
                CompounderJeiMode.fromHighShear(
                        screen.highShearEnabled()
                );

        return List.of(
                IGuiClickableArea.createBasic(
                        visual.x(),
                        visual.y(),
                        visual.width(),
                        visual.height(),
                        mode.recipeType()
                )
        );
    }
}
