package org.kdvcs.poiesis.machine.energy;

import org.kdvcs.poiesis.machine.definition.EnergySpec;

public final class VoltageOverclockPolicy
        implements OperatingPowerPolicy {

    private static final double MINIMUM_ENERGY_MULTIPLIER =
            0.25D;

    @Override
    public PowerAllocation allocate(
            EnergyComponent energy,
            EnergySpec machineSpec,
            int baseEnergyPerTick,
            int requestedSpeed,
            double energyMultiplier
    ) {
        int maximumSpeed =
                calculateMaximumSpeed(machineSpec);

        int speed = Math.min(
                Math.max(1, requestedSpeed),
                maximumSpeed
        );

        double effectiveEnergyMultiplier =
                Math.max(
                        MINIMUM_ENERGY_MULTIPLIER,
                        energyMultiplier
                );

        long rawCost =
                (long) Math.max(1, baseEnergyPerTick)
                        * speed
                        * speed;

        int energyPerTick =
                clampToPositiveInt(
                        Math.ceil(
                                rawCost
                                        * effectiveEnergyMultiplier
                        )
                );

        if (energy.stored() < energyPerTick) {
            return PowerAllocation.inactive(
                    PowerState.STARVED
            );
        }

        int operatingVoltage =
                Math.min(
                        machineSpec.maxOperatingVoltage(),
                        machineSpec.nominalVoltage()
                                * speed
                );

        return new PowerAllocation(
                true,
                speed,
                energyPerTick,
                operatingVoltage,
                speed > 1
                        ? PowerState.OVERCLOCKED
                        : PowerState.NOMINAL
        );
    }

    private static int calculateMaximumSpeed(
            EnergySpec spec
    ) {
        if (spec.nominalVoltage() <= 0) {
            return 1;
        }

        return Math.max(
                1,
                spec.maxOperatingVoltage()
                        / spec.nominalVoltage()
        );
    }

    private static int clampToPositiveInt(
            double value
    ) {
        if (value >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return Math.max(
                1,
                (int) value
        );
    }
}