package org.kdvcs.poiesis.compat.jei.handler;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiRecipeTypes;
import org.kdvcs.poiesis.screen.SeparatorScreen;
import org.kdvcs.poiesis.screen.layout.ProgressIndicatorLayout;
import org.kdvcs.poiesis.screen.layout.SeparatorGuiLayout;
import org.kdvcs.poiesis.compat.jei.util.JeiTankClickables;
import org.kdvcs.poiesis.compat.jei.util.TankSpec;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Opens the JEI page for the Separator's currently installed process module. */
public final class SeparatorGuiHandler
        implements IGuiContainerHandler<SeparatorScreen> {

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(
            SeparatorScreen screen,
            double guiMouseX,
            double guiMouseY
    ) {
        ProgressIndicatorLayout visual = SeparatorGuiLayout.PROGRESS;

        var recipeType = screen.getMenu().electrodeSlotEnabled()
                ? PoiesisJeiRecipeTypes.ELECTROSTATIC_SEPARATING
                : screen.getMenu().rinsingModuleInstalled()
                ? PoiesisJeiRecipeTypes.RINSING_SEPARATING
                : screen.getMenu().fluidSlotEnabled()
                ? PoiesisJeiRecipeTypes.WET_SEPARATING
                : PoiesisJeiRecipeTypes.DRY_SEPARATING;

        return List.of(IGuiClickableArea.createBasic(
                visual.x(),
                visual.y(),
                visual.width(),
                visual.height(),
                recipeType
        ));
    }

    @Override
    public Optional<IClickableIngredient<?>> getClickableIngredientUnderMouse(
            SeparatorScreen screen, double mouseX, double mouseY) {
        List<TankSpec> tanks = new ArrayList<>();
        tanks.add(new TankSpec(
                SeparatorGuiLayout.FLUID_X,
                SeparatorGuiLayout.FLUID_Y,
                SeparatorGuiLayout.FLUID_SIZE,
                SeparatorGuiLayout.FLUID_SIZE,
                screenObject -> ((SeparatorScreen) screenObject).getMenu().fluidStack()
        ));
        if (screen.getMenu().rinsingModuleInstalled()) {
            tanks.add(new TankSpec(
                    SeparatorGuiLayout.OUTPUT_X[3],
                    SeparatorGuiLayout.OUTPUT_Y[3],
                    SeparatorGuiLayout.FLUID_SIZE,
                    SeparatorGuiLayout.FLUID_SIZE,
                    screenObject -> ((SeparatorScreen) screenObject).getMenu().wastewaterStack()
            ));
        }

        return JeiTankClickables.findTankIngredientUnderMouse(
                screen, screen.getGuiLeft(), screen.getGuiTop(), mouseX, mouseY,
                tanks
        );
    }
}
