package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.runtime.IClickableIngredient;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.compat.jei.util.JeiTankClickables;
import org.kdvcs.poiesis.compat.jei.util.TankSpec;
import org.kdvcs.poiesis.screen.GreenhouseScreen;
import org.kdvcs.poiesis.screen.layout.GreenhouseGuiLayout;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class GreenhouseGuiHandler implements IGuiContainerHandler<GreenhouseScreen> {
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            GreenhouseScreen screen, double guiMouseX, double guiMouseY) {
        var progress = GreenhouseGuiLayout.PROGRESS;
        return List.of(IGuiClickableArea.createBasic(
                progress.x(), progress.y(), progress.width(), progress.height(),
                PoiesisJeiRecipeTypes.GREENHOUSE));
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
            GreenhouseScreen screen, double mouseX, double mouseY) {
        return JeiTankClickables.findTankIngredientUnderMouse(
                screen, screen.getGuiLeft(), screen.getGuiTop(), mouseX, mouseY,
                List.of(
                        new TankSpec(GreenhouseGuiLayout.WATER_X, GreenhouseGuiLayout.WATER_Y,
                                GreenhouseGuiLayout.FLUID_SIZE, GreenhouseGuiLayout.FLUID_SIZE,
                                value -> ((GreenhouseScreen) value).getMenu().waterStack()),
                        new TankSpec(GreenhouseGuiLayout.NUTRIENT_FLUID_X, GreenhouseGuiLayout.NUTRIENT_FLUID_Y,
                                GreenhouseGuiLayout.FLUID_SIZE, GreenhouseGuiLayout.FLUID_SIZE,
                                value -> ((GreenhouseScreen) value).getMenu().nutrientFluidStack()),
                        new TankSpec(GreenhouseGuiLayout.FLUID_OUTPUT_X, GreenhouseGuiLayout.FLUID_OUTPUT_Y,
                                GreenhouseGuiLayout.FLUID_SIZE, GreenhouseGuiLayout.FLUID_SIZE,
                                value -> ((GreenhouseScreen) value).getMenu().outputFluidStack())
                ));
    }
}
