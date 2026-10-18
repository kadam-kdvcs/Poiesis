package org.kdvcs.poiesis.machine.greenhouse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.kdvcs.poiesis.machine.api.MachineComponent;

/** Dedicated Greenhouse reservoir that can never contain anything except water. */
public final class GreenhouseWaterComponent implements MachineComponent {
    private final FluidTank tank;
    private long version;

    public GreenhouseWaterComponent(int capacity, Runnable changed) {
        tank = new FluidTank(capacity, stack -> stack.getFluid() == Fluids.WATER) {
            @Override
            protected void onContentsChanged() {
                version++;
                changed.run();
            }
        };
    }

    @Override
    public String key() {
        return "water_fluid";
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
