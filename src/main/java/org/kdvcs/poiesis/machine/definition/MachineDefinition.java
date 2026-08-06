package org.kdvcs.poiesis.machine.definition;

public record MachineDefinition(
        String id,
        EnergySpec energy,
        int inputSlots,
        int outputSlots,
        int moduleSlots
) {
    public MachineDefinition {
        if (inputSlots < 0
                || outputSlots < 0
                || moduleSlots < 0) {
            throw new IllegalArgumentException(
                    "Slot counts cannot be negative"
            );
        }
    }
}
