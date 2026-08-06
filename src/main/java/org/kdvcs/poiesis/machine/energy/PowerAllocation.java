package org.kdvcs.poiesis.machine.energy;

public record PowerAllocation(
        boolean active,
        int progressPerTick,
        int energyPerTick,
        int operatingVoltage,
        PowerState state
) {
    public static PowerAllocation inactive(PowerState state) {
        return new PowerAllocation(false, 0, 0, 0, state);
    }
}
