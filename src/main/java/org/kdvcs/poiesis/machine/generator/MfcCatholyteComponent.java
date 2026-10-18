package org.kdvcs.poiesis.machine.generator;

import net.minecraftforge.fluids.FluidStack;

import java.util.function.Predicate;

public final class MfcCatholyteComponent extends MfcFluidComponent {
    public MfcCatholyteComponent(int capacity, Predicate<FluidStack> validator, Runnable changed) {
        super("mfc_catholyte", capacity, validator, changed);
    }
}
