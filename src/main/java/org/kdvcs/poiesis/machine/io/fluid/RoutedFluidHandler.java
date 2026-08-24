package org.kdvcs.poiesis.machine.io.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/** A capability view that keeps side rules and dynamic module locks enforceable. */
public final class RoutedFluidHandler implements IFluidHandler {
    private final IFluidHandler delegate;
    private final BooleanSupplier enabled;
    private final BooleanSupplier allowsInput;
    private final BooleanSupplier allowsOutput;

    public RoutedFluidHandler(
            IFluidHandler delegate,
            BooleanSupplier enabled,
            BooleanSupplier allowsInput,
            BooleanSupplier allowsOutput
    ) {
        this.delegate = delegate;
        this.enabled = enabled;
        this.allowsInput = allowsInput;
        this.allowsOutput = allowsOutput;
    }

    @Override
    public int getTanks() {
        return enabled.getAsBoolean() ? delegate.getTanks() : 0;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return enabled.getAsBoolean()
                ? delegate.getFluidInTank(tank)
                : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {
        return enabled.getAsBoolean() ? delegate.getTankCapacity(tank) : 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return enabled.getAsBoolean()
                && allowsInput.getAsBoolean()
                && delegate.isFluidValid(tank, stack);
    }

    @Override
    public int fill(@NotNull FluidStack resource, FluidAction action) {
        if (!enabled.getAsBoolean() || !allowsInput.getAsBoolean()) {
            return 0;
        }
        return delegate.fill(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(
            @NotNull FluidStack resource,
            FluidAction action
    ) {
        if (!enabled.getAsBoolean() || !allowsOutput.getAsBoolean()) {
            return FluidStack.EMPTY;
        }
        return delegate.drain(resource, action);
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (!enabled.getAsBoolean() || !allowsOutput.getAsBoolean()) {
            return FluidStack.EMPTY;
        }
        return delegate.drain(maxDrain, action);
    }
}
