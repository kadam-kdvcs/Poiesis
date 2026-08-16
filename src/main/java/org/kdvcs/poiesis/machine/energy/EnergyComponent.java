package org.kdvcs.poiesis.machine.energy;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.definition.EnergySpec;

/**
 * Machine-side Forge Energy storage.
 *
 * This component deliberately does not infer an electrical "input voltage"
 * from calls to IEnergyStorage.receiveEnergy(). Forge Energy only tells a
 * receiver how much energy a sender is attempting to insert and how much can
 * currently be accepted; it does not expose the sender's true available power
 * or voltage. Near a full internal buffer, accepted FE can therefore be much
 * smaller than the source is capable of supplying.
 *
 * Overclocking is handled by OperatingPowerPolicy using the machine's own
 * operating tier and the energy actually available in this buffer. If Poiesis
 * later needs a real voltage network, that should be represented by a
 * Poiesis-specific power contract rather than inferred from Forge Energy.
 */
public final class EnergyComponent implements MachineComponent {
    private final EnergySpec spec;
    private final MachineEnergyStorage storage;
    private final LazyOptional<IEnergyStorage> capability;

    public EnergyComponent(
            EnergySpec spec,
            Runnable changed
    ) {
        this.spec = spec;
        this.storage = new MachineEnergyStorage(
                spec.capacity(),
                spec.maxReceive(),
                changed
        );

        this.capability = LazyOptional.of(() -> new IEnergyStorage() {
            @Override
            public int receiveEnergy(
                    int maxReceive,
                    boolean simulate
            ) {
                return storage.receiveEnergy(
                        maxReceive,
                        simulate
                );
            }

            @Override
            public int extractEnergy(
                    int maxExtract,
                    boolean simulate
            ) {
                return 0;
            }

            @Override
            public int getEnergyStored() {
                return storage.getEnergyStored();
            }

            @Override
            public int getMaxEnergyStored() {
                return storage.getMaxEnergyStored();
            }

            @Override
            public boolean canExtract() {
                return false;
            }

            @Override
            public boolean canReceive() {
                return true;
            }
        });
    }

    @Override
    public String key() {
        return "energy";
    }

    public EnergySpec spec() {
        return spec;
    }

    public IEnergyStorage view() {
        return capability.orElseThrow(
                IllegalStateException::new
        );
    }

    public LazyOptional<IEnergyStorage> capability() {
        return capability;
    }

    public int stored() {
        return storage.getEnergyStored();
    }

    public int capacity() {
        return storage.getMaxEnergyStored();
    }

    public boolean consume(int amount) {
        if (amount <= 0) {
            return true;
        }

        return storage.extractInternal(
                amount,
                false
        ) == amount;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt(
                "stored",
                stored()
        );
    }

    @Override
    public void load(CompoundTag tag) {
        storage.setEnergy(
                tag.getInt("stored")
        );

        // Old saves may still contain last_input_voltage / last_input_at.
        // They are intentionally ignored: those values were runtime
        // observations and should never influence a reloaded machine.
    }

    @Override
    public void invalidate() {
        capability.invalidate();
    }
}
