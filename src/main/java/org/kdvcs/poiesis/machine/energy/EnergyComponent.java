package org.kdvcs.poiesis.machine.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.definition.EnergySpec;

import java.util.function.LongSupplier;

public final class EnergyComponent implements MachineComponent {
    private final EnergySpec spec;
    private final MachineEnergyStorage storage;
    private final LongSupplier gameTime;
    private final LazyOptional<IEnergyStorage> capability;
    private int receivedThisTick;
    private long receivedAt = Long.MIN_VALUE;
    private int lastInputVoltage;
    private long lastInputAt = Long.MIN_VALUE;

    public EnergyComponent(EnergySpec spec, Runnable changed, LongSupplier gameTime) {
        this.spec = spec;
        this.gameTime = gameTime;
        this.storage = new MachineEnergyStorage(spec.capacity(), spec.maxReceive(), changed);
        this.capability = LazyOptional.of(() -> new IEnergyStorage() {
            @Override public int receiveEnergy(int maxReceive, boolean simulate) {
                int received = storage.receiveEnergy(maxReceive, simulate);
                if (!simulate && received > 0) recordInput(received);
                return received;
            }
            @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
            @Override public int getEnergyStored() { return storage.getEnergyStored(); }
            @Override public int getMaxEnergyStored() { return storage.getMaxEnergyStored(); }
            @Override public boolean canExtract() { return false; }
            @Override public boolean canReceive() { return true; }
        });
    }

    @Override public String key() { return "energy"; }
    public EnergySpec spec() { return spec; }
    public IEnergyStorage view() { return capability.orElseThrow(IllegalStateException::new); }
    public LazyOptional<IEnergyStorage> capability() { return capability; }
    public int stored() { return storage.getEnergyStored(); }
    public int capacity() { return storage.getMaxEnergyStored(); }
    public boolean consume(int amount) { return storage.extractInternal(amount, false) == amount; }

    private void recordInput(int amount) {
        long now = gameTime.getAsLong();
        if (receivedAt != now) {
            receivedAt = now;
            receivedThisTick = 0;
        }
        receivedThisTick += amount;
        lastInputVoltage = receivedThisTick;
        lastInputAt = now;
    }

    public int effectiveInputVoltage() {
        long now = gameTime.getAsLong();
        if (now == receivedAt) return receivedThisTick;
        if (lastInputAt != Long.MIN_VALUE && now - lastInputAt <= spec.voltageHoldTicks()) return lastInputVoltage;
        return 0;
    }

    @Override public void save(CompoundTag tag) {
        tag.putInt("stored", stored());
        tag.putInt("last_input_voltage", lastInputVoltage);
        tag.putLong("last_input_at", lastInputAt);
    }

    @Override public void load(CompoundTag tag) {
        storage.setEnergy(tag.getInt("stored"));
        lastInputVoltage = tag.getInt("last_input_voltage");
        lastInputAt = tag.getLong("last_input_at");
    }

    @Override public void invalidate() { capability.invalidate(); }
}
