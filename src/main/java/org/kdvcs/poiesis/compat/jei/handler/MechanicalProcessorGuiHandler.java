package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.kdvcs.poiesis.compat.jei.MechanicalProcessingJeiMode;
import org.kdvcs.poiesis.screen.MechanicalProcessorScreen;
import org.kdvcs.poiesis.screen.layout.MechanicalProcessorGuiLayout;
import org.kdvcs.poiesis.screen.layout.ProgressIndicatorLayout;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Makes the processor's actual progress arrow a JEI recipe button.
 *
 * The click target is dynamic: Pressing opens Pressing recipes, Grinding opens
 * Grinding recipes, and Extrusion opens Extrusion recipes. With no process
 * module installed there is no processing arrow, so there is no JEI click area.
 */
public final class MechanicalProcessorGuiHandler
        implements IGuiContainerHandler<MechanicalProcessorScreen> {

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            MechanicalProcessorScreen screen,
            double guiMouseX,
            double guiMouseY
    ) {
        Optional<MechanicalProcessingJeiMode> mode =
                MechanicalProcessingJeiMode.fromMachineMode(
                        screen.mode()
                );

        if (mode.isEmpty()) {
            return List.of();
        }

        ProgressIndicatorLayout visual =
                MechanicalProcessorGuiLayout.progressVisual(
                        mode.get().machineMode()
                );

        if (visual == null) {
            return List.of();
        }

        return List.of(
                IGuiClickableArea.createBasic(
                        visual.x(),
                        visual.y(),
                        visual.width(),
                        visual.height(),
                        mode.get().recipeType()
                )
        );
    }
}
