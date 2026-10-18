package org.kdvcs.poiesis.machine.io.fluid;

import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/** Two fill-only inputs and one drain-only output behind the Greenhouse fluid capability. */
public final class GreenhouseRoutedFluidHandler implements IFluidHandler {
    private final IFluidHandler water;
    private final IFluidHandler nutrient;
    private final IFluidHandler output;
    private final BooleanSupplier allowsInput;
    private final BooleanSupplier allowsOutput;

    public GreenhouseRoutedFluidHandler(
            IFluidHandler water,
            IFluidHandler nutrient,
            IFluidHandler output,
            BooleanSupplier allowsInput,
            BooleanSupplier allowsOutput
    ) {
        this.water = water;
        this.nutrient = nutrient;
        this.output = output;
        this.allowsInput = allowsInput;
        this.allowsOutput = allowsOutput;
    }

    @Override
    public int getTanks() {
        return 3;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return switch (tank) {
            case 0 -> water.getFluidInTank(0);
            case 1 -> nutrient.getFluidInTank(0);
            case 2 -> output.getFluidInTank(0);
            default -> FluidStack.EMPTY;
        };
    }

    @Override
    public int getTankCapacity(int tank) {
        return switch (tank) {
            case 0 -> water.getTankCapacity(0);
            case 1 -> nutrient.getTankCapacity(0);
            case 2 -> output.getTankCapacity(0);
            default -> 0;
        };
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (!allowsInput.getAsBoolean()) {
            return false;
        }
        return switch (tank) {
            case 0 -> stack.getFluid() == Fluids.WATER && water.isFluidValid(0, stack);
            case 1 -> nutrient.isFluidValid(0, stack);
            default -> false;
        };
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!allowsInput.getAsBoolean() || resource.isEmpty()) {
            return 0;
        }
        if (resource.getFluid() != Fluids.WATER) {
            return nutrient.fill(resource, action);
        }

        int waterAccepted = water.fill(resource, action);
        if (waterAccepted >= resource.getAmount()) {
            return waterAccepted;
        }
        FluidStack remainder = resource.copy();
        remainder.shrink(waterAccepted);
        return waterAccepted + nutrient.fill(remainder, action);
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return allowsOutput.getAsBoolean()
                ? output.drain(resource, action)
                : FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return allowsOutput.getAsBoolean()
                ? output.drain(maxDrain, action)
                : FluidStack.EMPTY;
    }
}
