package org.kdvcs.poiesis.machine.greenhouse;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.kdvcs.poiesis.machine.api.MachineComponent;

/** Recipe-driven Greenhouse nutrient solution tank. */
public final class GreenhouseNutrientFluidComponent implements MachineComponent {
    private final FluidTank tank;
    private long version;

    public GreenhouseNutrientFluidComponent(int capacity, Runnable changed) {
        tank = new FluidTank(capacity) {
            @Override
            protected void onContentsChanged() {
                version++;
                changed.run();
            }
        };
    }

    @Override
    public String key() {
        return "nutrient_fluid";
    }

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
