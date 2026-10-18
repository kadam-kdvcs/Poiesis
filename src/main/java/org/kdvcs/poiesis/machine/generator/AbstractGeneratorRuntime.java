package org.kdvcs.poiesis.machine.generator;

import net.minecraftforge.items.IItemHandler;
import org.kdvcs.poiesis.machine.MachineRuntime;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.BatterySpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.transfer.EnergyAutoTransfer;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleLayout;

import java.util.List;

/** Shared storage, charging, module and energy-output infrastructure for generators. */
public abstract class AbstractGeneratorRuntime extends MachineRuntime {
    private final GeneratorDefinition generatorDefinition;
    private final ModuleComponent modules;

    protected AbstractGeneratorRuntime(
            MachineContext owner,
            GeneratorDefinition generatorDefinition,
            MachineDefinition machineDefinition
    ) {
        super(owner, machineDefinition, (slot, stack) -> false);
        this.generatorDefinition = generatorDefinition;

        if (machineDefinition.moduleSlots() != ModuleComponent.REQUIRED_SLOT_COUNT) {
            throw new IllegalArgumentException(
                    "Current generator GUI requires exactly three module slots"
            );
        }

        modules = addComponent(
                ModuleComponent.class,
                new ModuleComponent(ModuleLayout.disabledGeneratorSlots(), owner::markPersistentDirty)
        );
    }

    @Override
    protected final void tickProcess() {
        tickGeneration();

        if (autoIo().orElseThrow().enabled(IoChannel.ENERGY, AutoIoDirection.OUTPUT)) {
            EnergyAutoTransfer.pushEnergy(
                    owner,
                    energy(),
                    sideConfiguration().orElseThrow(),
                    outputPower()
            );
        }
    }

    protected abstract void tickGeneration();

    public final int outputPower() {
        // Generator-specific performance modules will modify this later.
        return generatorDefinition.outputPower();
    }

    public final GeneratorDefinition generatorDefinition() {
        return generatorDefinition;
    }

    public final ModuleComponent modules() {
        return modules;
    }

    @Override
    protected final BatterySlotComponent createBatteryComponent(
            BatterySpec spec,
            EnergyComponent energy,
            Runnable changed
    ) {
        // Generator battery slots charge inserted items from internal storage.
        return new BatterySlotComponent(spec, energy, changed, true);
    }

    @Override
    protected List<IItemHandler> additionalDropHandlers() {
        return List.of(modules.items());
    }
}
