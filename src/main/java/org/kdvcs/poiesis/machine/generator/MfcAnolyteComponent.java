package org.kdvcs.poiesis.machine.generator;

import net.minecraftforge.fluids.FluidStack;

import java.util.function.Predicate;

public final class MfcAnolyteComponent extends MfcFluidComponent {
    public MfcAnolyteComponent(int capacity, Predicate<FluidStack> validator, Runnable changed) {
        super("mfc_anolyte", capacity, validator, changed);
    }
}
