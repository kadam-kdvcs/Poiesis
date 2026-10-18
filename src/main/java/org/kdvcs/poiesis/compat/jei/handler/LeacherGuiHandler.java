package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.screen.LeacherScreen;
import org.kdvcs.poiesis.screen.layout.LeacherGuiLayout;
import org.kdvcs.poiesis.compat.jei.util.JeiTankClickables;
import org.kdvcs.poiesis.compat.jei.util.TankSpec;
import mezz.jei.api.runtime.IClickableIngredient;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class LeacherGuiHandler implements IGuiContainerHandler<LeacherScreen> {
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            LeacherScreen screen, double guiMouseX, double guiMouseY) {
        var progress = LeacherGuiLayout.PROGRESS;
        return List.of(
                IGuiClickableArea.createBasic(
                        progress.x(), progress.y(), progress.width(), progress.height(),
                        PoiesisJeiRecipeTypes.LEACHING
                )
        );
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
            LeacherScreen screen, double mouseX, double mouseY) {
        return JeiTankClickables.findTankIngredientUnderMouse(
                screen, screen.getGuiLeft(), screen.getGuiTop(), mouseX, mouseY,
                List.of(
                        new TankSpec(LeacherGuiLayout.FLUID_INPUT_X,
                                LeacherGuiLayout.FLUID_INPUT_Y,
                                LeacherGuiLayout.FLUID_SIZE,
                                LeacherGuiLayout.FLUID_SIZE,
                                value -> ((LeacherScreen) value).getMenu().inputFluidStack()),
                        new TankSpec(LeacherGuiLayout.FLUID_OUTPUT_X,
                                LeacherGuiLayout.FLUID_OUTPUT_Y,
                                LeacherGuiLayout.FLUID_SIZE,
                                LeacherGuiLayout.FLUID_SIZE,
                                value -> ((LeacherScreen) value).getMenu().outputFluidStack())
                )
        );
    }
}
