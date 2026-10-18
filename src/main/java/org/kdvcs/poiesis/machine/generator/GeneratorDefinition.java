package org.kdvcs.poiesis.machine.generator;

import org.kdvcs.poiesis.machine.definition.BatterySpec;
import org.kdvcs.poiesis.machine.definition.EnergySpec;
import org.kdvcs.poiesis.machine.definition.FluidSpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.MachineIoSpec;

/** Shared construction-time values for every Poiesis generator. */
public record GeneratorDefinition(
        String id,
        int energyCapacity,
        int outputPower,
        int baseGenerationPerTick,
        int fuelCapacity,
        int batteryTransferPerTick,
        int moduleSlots
) {
    public GeneratorDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Generator id cannot be blank");
        }
        if (energyCapacity <= 0 || outputPower <= 0
                || baseGenerationPerTick <= 0 || fuelCapacity <= 0
                || batteryTransferPerTick <= 0 || moduleSlots < 0) {
            throw new IllegalArgumentException("Generator values must be positive");
        }
    }

    public MachineDefinition machineDefinition() {
        return new MachineDefinition(
                id,
                new EnergySpec(
                        energyCapacity,
                        batteryTransferPerTick,
                        outputPower,
                        baseGenerationPerTick,
                        baseGenerationPerTick,
                        baseGenerationPerTick,
                        1
                ),
                0,
                0,
                moduleSlots,
                MachineIoSpec.builder()
                        .fluidInput()
                        .energyOutput()
                        .build(),
                new BatterySpec(batteryTransferPerTick),
                new FluidSpec(fuelCapacity)
        );
    }
}
