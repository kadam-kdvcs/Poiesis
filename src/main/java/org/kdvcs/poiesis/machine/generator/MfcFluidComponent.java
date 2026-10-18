package org.kdvcs.poiesis.machine.generator;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.kdvcs.poiesis.machine.api.MachineComponent;

import java.util.function.Predicate;

/** A named, recipe-filtered MFC chamber reservoir. */
public class MfcFluidComponent implements MachineComponent {
    private final String key;
    private final FluidTank tank;
    private long version;

    public MfcFluidComponent(
            String key,
            int capacity,
            Predicate<FluidStack> validator,
            Runnable changed
    ) {
        this.key = key;
        tank = new FluidTank(capacity, validator) {
            @Override
            protected void onContentsChanged() {
                version++;
                changed.run();
            }
        };
    }

    @Override public String key() { return key; }
    public FluidTank tank() { return tank; }
    public FluidStack fluid() { return tank.getFluid(); }
    public int amount() { return tank.getFluidAmount(); }
    public int capacity() { return tank.getCapacity(); }

    @Override
    public void save(CompoundTag tag) {
        tank.writeToNBT(tag);
        tag.putLong("version", version);
    }

    @Override
    public void load(CompoundTag tag) {
        tank.readFromNBT(tag);
        version = tag.getLong("version");
    }
}
