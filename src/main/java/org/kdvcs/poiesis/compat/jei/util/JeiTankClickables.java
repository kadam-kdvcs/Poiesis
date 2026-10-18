package org.kdvcs.poiesis.compat.jei.util;

import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraftforge.fluids.FluidStack;
import org.kdvcs.poiesis.compat.jei.PoiesisJeiPlugin;

import java.util.List;
import java.util.Optional;

public final class JeiTankClickables {
    private JeiTankClickables() { }

    public static Optional<IClickableIngredient<?>> findTankIngredientUnderMouse(
            Object screen,
            int guiLeft,
            int guiTop,
            double mouseX,
            double mouseY,
            List<TankSpec> tanks
    ) {
        var manager = PoiesisJeiPlugin.ingredientManager();
        if (manager == null) {
            return Optional.empty();
        }

        for (TankSpec tank : tanks) {
            int x = guiLeft + tank.relX();
            int y = guiTop + tank.relY();
            if (mouseX < x || mouseX >= x + tank.width()
                    || mouseY < y || mouseY >= y + tank.height()) {
                continue;
            }

            FluidStack fluid = tank.fluidGetter().apply(screen);
            if (fluid == null || fluid.isEmpty()) {
                continue;
            }

            Optional<ITypedIngredient<FluidStack>> typed = manager.createTypedIngredient(
                    ForgeTypes.FLUID_STACK,
                    fluid.copy()
            );
            if (typed.isEmpty()) {
                continue;
            }

            Rect2i area = new Rect2i(x, y, tank.width(), tank.height());
            ITypedIngredient<FluidStack> ingredient = typed.get();
            return Optional.of(new IClickableIngredient<FluidStack>() {
                @Override
                public ITypedIngredient<FluidStack> getTypedIngredient() {
                    return ingredient;
                }

                @Override
                public Rect2i getArea() {
                    return area;
                }
            });
        }
        return Optional.empty();
    }
}
