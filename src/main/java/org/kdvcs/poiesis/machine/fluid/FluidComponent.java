package org.kdvcs.poiesis.machine.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.definition.FluidSpec;

public final class FluidComponent implements MachineComponent {
    private final FluidTank tank;
    private long version;

    public FluidComponent(FluidSpec spec, Runnable changed) {
        tank = new FluidTank(spec.capacity()) {
            @Override
            protected void onContentsChanged() {
                version++;
                changed.run();
            }
        };
    }

    @Override
    public String key() {
        return "fluid";
    }

    public FluidTank tank() {
        return tank;
    }

    public FluidStack fluid() {
        return tank.getFluid();
    }

    public int amount() {
        return tank.getFluidAmount();
    }

    public int capacity() {
        return tank.getCapacity();
    }

    public long version() {
        return version;
    }

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
