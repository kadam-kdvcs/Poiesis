package org.kdvcs.poiesis.machine.io.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/** Fill-only input tank plus drain-only output tank behind one side capability. */
public final class DualTankRoutedFluidHandler implements IFluidHandler {
    private final IFluidHandler input;
    private final IFluidHandler output;
    private final BooleanSupplier allowsInput;
    private final BooleanSupplier allowsOutput;

    public DualTankRoutedFluidHandler(
            IFluidHandler input,
            IFluidHandler output,
            BooleanSupplier allowsInput,
            BooleanSupplier allowsOutput
    ) {
        this.input = input;
        this.output = output;
        this.allowsInput = allowsInput;
        this.allowsOutput = allowsOutput;
    }

    @Override public int getTanks() { return 2; }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return tank == 0 ? input.getFluidInTank(0)
                : tank == 1 ? output.getFluidInTank(0) : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? input.getTankCapacity(0)
                : tank == 1 ? output.getTankCapacity(0) : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return tank == 0 && allowsInput.getAsBoolean() && input.isFluidValid(0, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return allowsInput.getAsBoolean() ? input.fill(resource, action) : 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return allowsOutput.getAsBoolean()
                ? output.drain(resource, action) : FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return allowsOutput.getAsBoolean()
                ? output.drain(maxDrain, action) : FluidStack.EMPTY;
    }
}
