package org.kdvcs.poiesis.compat.jade;

import net.minecraft.nbt.CompoundTag;
import org.kdvcs.poiesis.block.entity.AdsorptionColumnControllerBlockEntity;
import org.kdvcs.poiesis.block.entity.base.AbstractMachineBlockEntity;
import org.kdvcs.poiesis.machine.AdsorptionColumnRuntime;
import org.kdvcs.poiesis.machine.BiomassGasifierRuntime;
import org.kdvcs.poiesis.machine.LeacherRuntime;
import org.kdvcs.poiesis.machine.MachineRuntime;
import org.kdvcs.poiesis.machine.MaterialMachineRuntime;
import org.kdvcs.poiesis.machine.SeparatorRuntime;
import org.kdvcs.poiesis.machine.alloy.AlloyFactoryRuntime;
import org.kdvcs.poiesis.machine.fabrication.ModuleFabricatorRuntime;
import org.kdvcs.poiesis.machine.generator.GeneratorRuntime;
import org.kdvcs.poiesis.machine.generator.MicrobialFuelCellRuntime;
import org.kdvcs.poiesis.machine.greenhouse.GreenhouseRuntime;

import java.util.List;

import static org.kdvcs.poiesis.compat.jade.JadeDataKeys.*;

/** Open adapter registry for machine-specific Jade server snapshots. */
final class JadeMachineAdapters {
    private static final List<Adapter> ADAPTERS = List.of(
            new AlloyFactoryAdapter(),
            new MaterialMachineAdapter(),
            new SeparatorAdapter(),
            new GreenhouseAdapter(),
            new LeacherAdapter(),
            new FabricatorAdapter(),
            new AdsorptionAdapter(),
            new GasifierAdapter(),
            new MicrobialFuelCellAdapter(),
            new GeneratorAdapter()
    );

    private JadeMachineAdapters() {
    }

    static void append(CompoundTag tag, AbstractMachineBlockEntity<?> machine) {
        MachineRuntime runtime = machine.runtime();
        for (Adapter adapter : ADAPTERS) {
            if (adapter.supports(runtime, machine)) {
                adapter.append(tag, runtime, machine);
                return;
            }
        }
    }

    private interface Adapter {
        boolean supports(MachineRuntime runtime, AbstractMachineBlockEntity<?> machine);

        void append(
                CompoundTag tag,
                MachineRuntime runtime,
                AbstractMachineBlockEntity<?> machine
        );
    }

    private static final class AlloyFactoryAdapter implements Adapter {
        public boolean supports(MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            return runtime instanceof AlloyFactoryRuntime;
        }

        public void append(CompoundTag tag, MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            AlloyFactoryRuntime alloy = (AlloyFactoryRuntime) runtime;
            JadeDataWriter.process(tag, alloy.process().progress(), alloy.process().maxProgress(),
                    alloy.process().activeEnergyPerTick());
            JadeDataWriter.modules(tag, alloy.modules());
            tag.putBoolean(AUTO_SMELTING, alloy.process().autoCrafting());
        }
    }

    private static final class MaterialMachineAdapter implements Adapter {
        public boolean supports(MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            return runtime instanceof MaterialMachineRuntime<?>;
        }

        public void append(CompoundTag tag, MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            MaterialMachineRuntime<?> material = (MaterialMachineRuntime<?>) runtime;
            JadeDataWriter.process(tag, material.process().progress(), material.process().maxProgress(),
                    material.process().activeEnergyPerTick());
            JadeDataWriter.modules(tag, material.modules());
        }
    }

    private static final class SeparatorAdapter implements Adapter {
        public boolean supports(MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            return runtime instanceof SeparatorRuntime;
        }

        public void append(CompoundTag tag, MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            SeparatorRuntime separator = (SeparatorRuntime) runtime;
            JadeDataWriter.process(tag, separator.process().progress(), separator.process().maxProgress(),
                    separator.process().activeEnergyPerTick());
            JadeDataWriter.modules(tag, separator.modules());
            if (separator.rinsingModuleInstalled()) {
                JadeDataWriter.dualFluid(
                        tag,
                        separator.fluid().orElseThrow().fluid(),
                        separator.outputFluid().fluid(),
                        separator.fluid().orElseThrow().capacity(),
                        separator.outputFluid().capacity()
                );
            }
        }
    }

    private static final class GreenhouseAdapter implements Adapter {
        public boolean supports(MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            return runtime instanceof GreenhouseRuntime;
        }

        public void append(CompoundTag tag, MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            GreenhouseRuntime greenhouse = (GreenhouseRuntime) runtime;
            JadeDataWriter.process(tag, greenhouse.process().progress(), greenhouse.process().maxProgress(),
                    greenhouse.process().activeEnergyPerTick());
            JadeDataWriter.modules(tag, greenhouse.modules());
            JadeDataWriter.greenhouse(tag, greenhouse);
        }
    }

    private static final class LeacherAdapter implements Adapter {
        public boolean supports(MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            return runtime instanceof LeacherRuntime;
        }

        public void append(CompoundTag tag, MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            LeacherRuntime leacher = (LeacherRuntime) runtime;
            JadeDataWriter.process(tag, leacher.process().progress(), leacher.process().maxProgress(),
                    leacher.process().activeEnergyPerTick());
            JadeDataWriter.modules(tag, leacher.modules());
            JadeDataWriter.dualFluid(tag, leacher.runtimeInputFluid().fluid(),
                    leacher.outputFluid().fluid(), leacher.runtimeInputFluid().capacity(),
                    leacher.outputFluid().capacity());
        }
    }

    private static final class FabricatorAdapter implements Adapter {
        public boolean supports(MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            return runtime instanceof ModuleFabricatorRuntime;
        }

        public void append(CompoundTag tag, MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            var process = ((ModuleFabricatorRuntime) runtime).process();
            JadeDataWriter.process(tag, process.progress(), process.maxProgress(),
                    process.activeEnergyPerTick());
            tag.putBoolean(AUTO_CRAFTING, process.autoCrafting());
        }
    }

    private static final class AdsorptionAdapter implements Adapter {
        public boolean supports(MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            return runtime instanceof AdsorptionColumnRuntime
                    && machine instanceof AdsorptionColumnControllerBlockEntity;
        }

        public void append(CompoundTag tag, MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            AdsorptionColumnRuntime adsorber = (AdsorptionColumnRuntime) runtime;
            AdsorptionColumnControllerBlockEntity controller =
                    (AdsorptionColumnControllerBlockEntity) machine;
            JadeDataWriter.process(tag, adsorber.process().progress(), adsorber.process().maxProgress(),
                    adsorber.process().activeEnergyPerTick());
            tag.putBoolean(FORMED, controller.isFormed());
            tag.putInt(BED_COUNT, controller.bedCount());
            JadeDataWriter.dualFluid(tag, controller.dirtyInputTank().getFluid(),
                    controller.cleanOutputTank().getFluid(),
                    controller.dirtyInputTank().getCapacity(),
                    controller.cleanOutputTank().getCapacity());
        }
    }

    private static final class GasifierAdapter implements Adapter {
        public boolean supports(MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            return runtime instanceof BiomassGasifierRuntime;
        }

        public void append(CompoundTag tag, MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            BiomassGasifierRuntime gasifier = (BiomassGasifierRuntime) runtime;
            JadeDataWriter.process(tag, gasifier.process().progress(), gasifier.process().maxProgress(), 0);
            tag.putInt(TEMPERATURE, gasifier.process().temperature());
        }
    }

    private static final class GeneratorAdapter implements Adapter {
        public boolean supports(MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            return runtime instanceof GeneratorRuntime<?>;
        }

        public void append(CompoundTag tag, MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            GeneratorRuntime<?> generator = (GeneratorRuntime<?>) runtime;
            int total = generator.process().burnTotal();
            JadeDataWriter.process(tag, Math.max(0, total - generator.process().burnRemaining()),
                    total, generator.process().activeGenerationPerTick());
            JadeDataWriter.modules(tag, generator.modules());
        }
    }

    private static final class MicrobialFuelCellAdapter implements Adapter {
        public boolean supports(MachineRuntime runtime, AbstractMachineBlockEntity<?> machine) {
            return runtime instanceof MicrobialFuelCellRuntime;
        }

        public void append(
                CompoundTag tag,
                MachineRuntime runtime,
                AbstractMachineBlockEntity<?> machine
        ) {
            MicrobialFuelCellRuntime fuelCell = (MicrobialFuelCellRuntime) runtime;
            JadeDataWriter.process(
                    tag,
                    fuelCell.process().progress(),
                    fuelCell.process().maxProgress(),
                    fuelCell.process().activeGenerationPerTick()
            );
            JadeDataWriter.modules(tag, fuelCell.modules());
            JadeDataWriter.microbialFuelCell(tag, fuelCell);
        }
    }
}
