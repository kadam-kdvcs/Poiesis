package org.kdvcs.poiesis.machine.energy;

import org.kdvcs.poiesis.machine.definition.EnergySpec;

/**
 * Operating-tier policy for modular machine overclocking.
 *
 * Performance modules request a speed tier. The machine definition limits the
 * highest legal tier, and the tier determines both progress per tick and
 * operating voltage. Energy cost grows quadratically with speed.
 *
 * The policy intentionally does not try to derive source voltage from Forge
 * Energy receive calls. Forge Energy has no standard voltage/power metadata,
 * and accepted FE is buffer-dependent. Using it as a voltage signal caused a
 * full machine to become permanently stuck at 1x until it was replaced.
 *
 * A machine can therefore overclock from its internal buffer as long as it can
 * pay the current tick's cost. If the external supply cannot sustain that
 * demand, the buffer drains naturally and the process becomes energy-starved.
 */
public final class VoltageOverclockPolicy
        implements OperatingPowerPolicy {

    private static final double MINIMUM_ENERGY_MULTIPLIER = 0.25D;

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

        double effectiveEnergyMultiplier = Math.max(
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

        int operatingVoltage = Math.min(
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
