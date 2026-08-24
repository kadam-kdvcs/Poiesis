package org.kdvcs.poiesis.machine.definition;

import org.kdvcs.poiesis.machine.io.MachineIoSpec;

public record MachineDefinition(
        String id,
        EnergySpec energy,
        int inputSlots,
        int outputSlots,
        int moduleSlots,
        MachineIoSpec io,
        BatterySpec battery,
        FluidSpec fluid
) {
    public MachineDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Machine id cannot be blank"
            );
        }

        if (energy == null || io == null) {
            throw new IllegalArgumentException(
                    "Machine definition values cannot be null"
            );
        }

        if (inputSlots < 0
                || outputSlots < 0
                || moduleSlots < 0) {
            throw new IllegalArgumentException(
                    "Slot counts cannot be negative"
            );
        }
    }

    public MachineDefinition(
            String id,
            EnergySpec energy,
            int inputSlots,
            int outputSlots,
            int moduleSlots,
            MachineIoSpec io,
            BatterySpec battery
    ) {
        this(
                id,
                energy,
                inputSlots,
                outputSlots,
                moduleSlots,
                io,
                battery,
                null
        );
    }

    public MachineDefinition(
            String id,
            EnergySpec energy,
            int inputSlots,
            int outputSlots,
            int moduleSlots,
            MachineIoSpec io
    ) {
        this(
                id,
                energy,
                inputSlots,
                outputSlots,
                moduleSlots,
                io,
                null,
                null
        );
    }

    public MachineDefinition(
            String id,
            EnergySpec energy,
            int inputSlots,
            int outputSlots,
            int moduleSlots
    ) {
        this(
                id,
                energy,
                inputSlots,
                outputSlots,
                moduleSlots,
                MachineIoSpec.none(),
                null,
                null
        );
    }

    public boolean hasBatterySlot() {
        return battery != null;
    }

    public boolean hasFluidTank() {
        return fluid != null;
    }
}
