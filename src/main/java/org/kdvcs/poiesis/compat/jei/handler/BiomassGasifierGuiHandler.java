package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.screen.BiomassGasifierScreen;
import org.kdvcs.poiesis.screen.layout.BiomassGasifierGuiLayout;
import org.kdvcs.poiesis.compat.jei.util.JeiTankClickables;
import org.kdvcs.poiesis.compat.jei.util.TankSpec;
import mezz.jei.api.runtime.IClickableIngredient;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Opens the gasification page from the flame in the machine GUI. */
public final class BiomassGasifierGuiHandler
        implements IGuiContainerHandler<BiomassGasifierScreen> {
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            BiomassGasifierScreen screen,
            double guiMouseX,
            double guiMouseY
    ) {
        return List.of(
                IGuiClickableArea.createBasic(
                        BiomassGasifierGuiLayout.FIRE_X,
                        BiomassGasifierGuiLayout.FIRE_Y,
                        BiomassGasifierGuiLayout.FIRE_SIZE,
                        BiomassGasifierGuiLayout.FIRE_SIZE,
                        PoiesisJeiRecipeTypes.GASIFICATION
                )
        );
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
            BiomassGasifierScreen screen, double mouseX, double mouseY) {
        return JeiTankClickables.findTankIngredientUnderMouse(
                screen, screen.getGuiLeft(), screen.getGuiTop(), mouseX, mouseY,
                List.of(new TankSpec(
                        BiomassGasifierGuiLayout.GAS_X,
                        BiomassGasifierGuiLayout.GAS_Y,
                        BiomassGasifierGuiLayout.GAS_SIZE,
                        BiomassGasifierGuiLayout.GAS_SIZE,
                        value -> ((BiomassGasifierScreen) value).getMenu().gasStack()
                ))
        );
    }
}
