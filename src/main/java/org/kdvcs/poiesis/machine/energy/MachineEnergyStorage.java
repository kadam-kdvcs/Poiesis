package org.kdvcs.poiesis.machine.energy;

import net.minecraftforge.energy.EnergyStorage;

public final class MachineEnergyStorage extends EnergyStorage {
    private final Runnable changed;

    public MachineEnergyStorage(
            int capacity,
            int maxReceive,
            int maxExtract,
            Runnable changed
    ) {
        super(capacity, maxReceive, maxExtract);
        this.changed = changed;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (!simulate && received > 0) changed.run();
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (!simulate && extracted > 0) changed.run();
        return extracted;
    }

    public int extractInternal(int amount, boolean simulate) {
        int extracted = Math.min(energy, Math.max(0, amount));
        if (!simulate && extracted > 0) {
            energy -= extracted;
            changed.run();
        }
        return extracted;
    }

    public int receiveInternal(int amount, boolean simulate) {
        int received = Math.min(capacity - energy, Math.max(0, amount));
        if (!simulate && received > 0) {
            energy += received;
            changed.run();
        }
        return received;
    }

    public void setEnergy(int value) {
        energy = Math.max(0, Math.min(capacity, value));
        changed.run();
    }
}
