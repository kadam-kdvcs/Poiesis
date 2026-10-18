package org.kdvcs.poiesis.compat.jei.util;

import net.minecraftforge.fluids.FluidStack;

import java.util.function.Function;

public record TankSpec(
        int relX,
        int relY,
        int width,
        int height,
        Function<Object, FluidStack> fluidGetter
) { }
