package org.kdvcs.poiesis.machine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import org.kdvcs.poiesis.block.entity.MechanicalProcessorBlockEntity;
import org.kdvcs.poiesis.machine.api.ComponentContainer;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.energy.VoltageOverclockPolicy;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.process.ProcessComponent;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipe;
import org.kdvcs.poiesis.registry.ModRecipeTypes;

import java.util.Optional;

public final class MechanicalMachineRuntime {
    private final MechanicalProcessorBlockEntity owner;
    private final ComponentContainer components =
            new ComponentContainer();

    private final InventoryComponent inventory;
    private final ModuleComponent modules;
    private final EnergyComponent energy;
    private final ProcessComponent process;

    public MechanicalMachineRuntime(
            MechanicalProcessorBlockEntity owner,
            MachineDefinition definition
    ) {
        this.owner = owner;

        Runnable changed = owner::setChanged;

        inventory = components.add(
                InventoryComponent.class,
                new InventoryComponent(
                        definition.inputSlots()
                                + definition.outputSlots(),
                        slot -> slot >= definition.inputSlots(),
                        changed
                )
        );

        modules = components.add(
                ModuleComponent.class,
                new ModuleComponent(
                        definition.moduleSlots(),
                        changed
                )
        );

        energy = components.add(
                EnergyComponent.class,
                new EnergyComponent(
                        definition.energy(),
                        changed,
                        () -> owner.getLevel() == null
                                ? 0L
                                : owner.getLevel().getGameTime()
                )
        );

        process = components.add(
                ProcessComponent.class,
                new ProcessComponent(
                        inventory,
                        modules,
                        energy,
                        new VoltageOverclockPolicy(),
                        this::findRecipe
                )
        );
    }

    private Optional<MechanicalProcessingRecipe> findRecipe() {
        if (owner.getLevel() == null) {
            return Optional.empty();
        }

        SimpleContainer container =
                new SimpleContainer(
                        inventory.items()
                                .getStackInSlot(0)
                );

        return owner.getLevel()
                .getRecipeManager()
                .getRecipeFor(
                        ModRecipeTypes
                                .MECHANICAL_PROCESSING
                                .get(),
                        container,
                        owner.getLevel()
                );
    }

    public void tickServer() {
        process.tick(
                owner::setChanged,
                owner::setWorking
        );
    }

    public void save(CompoundTag tag) {
        tag.putInt("machine_data_version", 2);
        components.save(tag);
    }

    public void load(CompoundTag tag) {
        components.load(tag);
    }

    public void invalidate() {
        components.invalidate();
    }

    public InventoryComponent inventory() {
        return inventory;
    }

    public ModuleComponent modules() {
        return modules;
    }

    public EnergyComponent energy() {
        return energy;
    }

    public ProcessComponent process() {
        return process;
    }
}
