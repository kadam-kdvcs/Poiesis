package org.kdvcs.poiesis.machine.io.fluid;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.function.BooleanSupplier;

/** A fill-only MFC capability routed to one chamber (or both for unsided access). */
public final class MfcRoutedFluidHandler implements IFluidHandler {
    public enum Chamber {
        ALL,
        ANOLYTE,
        CATHOLYTE
    }

    private final IFluidHandler anolyte;
    private final IFluidHandler catholyte;
    private final BooleanSupplier input;
    private final Chamber chamber;

    public MfcRoutedFluidHandler(
            IFluidHandler anolyte,
            IFluidHandler catholyte,
            BooleanSupplier input,
            Chamber chamber
    ) {
        this.anolyte = anolyte;
        this.catholyte = catholyte;
        this.input = input;
        this.chamber = chamber;
    }

    @Override
    public int getTanks() {
        if (!input.getAsBoolean()) return 0;
        return chamber == Chamber.ALL ? 2 : 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        if (!input.getAsBoolean()) return FluidStack.EMPTY;
        if (chamber == Chamber.ANOLYTE) return tank == 0
                ? anolyte.getFluidInTank(0) : FluidStack.EMPTY;
        if (chamber == Chamber.CATHOLYTE) return tank == 0
                ? catholyte.getFluidInTank(0) : FluidStack.EMPTY;
        return switch (tank) {
            case 0 -> anolyte.getFluidInTank(0);
            case 1 -> catholyte.getFluidInTank(0);
            default -> FluidStack.EMPTY;
        };
    }

    @Override
    public int getTankCapacity(int tank) {
        if (!input.getAsBoolean()) return 0;
        if (chamber == Chamber.ANOLYTE) return tank == 0
                ? anolyte.getTankCapacity(0) : 0;
        if (chamber == Chamber.CATHOLYTE) return tank == 0
                ? catholyte.getTankCapacity(0) : 0;
        return switch (tank) {
            case 0 -> anolyte.getTankCapacity(0);
            case 1 -> catholyte.getTankCapacity(0);
            default -> 0;
        };
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        if (!input.getAsBoolean() || tank < 0 || tank >= getTanks()) return false;
        if (chamber == Chamber.ANOLYTE) return anolyte.isFluidValid(0, stack);
        if (chamber == Chamber.CATHOLYTE) return catholyte.isFluidValid(0, stack);
        return switch (tank) {
            case 0 -> anolyte.isFluidValid(0, stack);
            case 1 -> catholyte.isFluidValid(0, stack);
            default -> false;
        };
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!input.getAsBoolean() || resource.isEmpty()) return 0;

        if (chamber == Chamber.ANOLYTE) {
            return anolyte.fill(resource, action);
        }
        if (chamber == Chamber.CATHOLYTE) {
            return catholyte.fill(resource, action);
        }

        FluidStack anolyteStored = anolyte.getFluidInTank(0);
        if (!anolyteStored.isEmpty() && anolyteStored.isFluidEqual(resource)) {
            return anolyte.fill(resource, action);
        }
        FluidStack catholyteStored = catholyte.getFluidInTank(0);
        if (!catholyteStored.isEmpty() && catholyteStored.isFluidEqual(resource)) {
            return catholyte.fill(resource, action);
        }

        boolean validAnolyte = anolyte.isFluidValid(0, resource);
        boolean validCatholyte = catholyte.isFluidValid(0, resource);
        boolean anolyteEmpty = anolyteStored.isEmpty();
        boolean catholyteEmpty = catholyteStored.isEmpty();
        if (validAnolyte && anolyteEmpty && !catholyteEmpty) {
            return anolyte.fill(resource, action);
        }
        if (validCatholyte && catholyteEmpty && !anolyteEmpty) {
            return catholyte.fill(resource, action);
        }
        if (validAnolyte && anolyteEmpty) return anolyte.fill(resource, action);
        return validCatholyte && catholyteEmpty ? catholyte.fill(resource, action) : 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
    }
}
