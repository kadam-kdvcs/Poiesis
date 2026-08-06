package org.kdvcs.poiesis.machine.energy;

import org.kdvcs.poiesis.machine.definition.EnergySpec;

public interface OperatingPowerPolicy {

    PowerAllocation allocate(
            EnergyComponent energy,
            EnergySpec machineSpec,
            int baseEnergyPerTick,
            int requestedSpeed,
            double energyMultiplier
    );
}