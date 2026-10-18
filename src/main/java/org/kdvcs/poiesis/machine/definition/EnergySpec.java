package org.kdvcs.poiesis.machine.definition;

public record EnergySpec(
        int capacity,
        int maxReceive,
        int maxExtract,
        int baseEnergyPerTick,
        int nominalVoltage,
        int maxOperatingVoltage,
        int voltageHoldTicks
) {
    public EnergySpec {
        if (capacity <= 0 || maxReceive < 0 || maxExtract < 0
                || baseEnergyPerTick <= 0) {
            throw new IllegalArgumentException("Energy values must be positive");
        }
        if (nominalVoltage < baseEnergyPerTick || maxOperatingVoltage < nominalVoltage) {
            throw new IllegalArgumentException("Invalid operating voltage range");
        }
    }

    /** Existing consuming machines receive energy but never expose extraction. */
    public EnergySpec(
            int capacity,
            int maxReceive,
            int baseEnergyPerTick,
            int nominalVoltage,
            int maxOperatingVoltage,
            int voltageHoldTicks
    ) {
        this(
                capacity,
                maxReceive,
                0,
                baseEnergyPerTick,
                nominalVoltage,
                maxOperatingVoltage,
                voltageHoldTicks
        );
    }
}
