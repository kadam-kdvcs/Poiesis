package org.kdvcs.poiesis.machine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import org.kdvcs.poiesis.machine.api.ComponentContainer;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.energy.VoltageOverclockPolicy;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.io.AutoIoComponent;
import org.kdvcs.poiesis.machine.io.MachineCapabilityRouter;
import org.kdvcs.poiesis.machine.io.SideConfigurationComponent;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.transfer.EnergyAutoTransfer;
import org.kdvcs.poiesis.machine.io.transfer.ItemAutoTransfer;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.process.ProcessComponent;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipe;
import org.kdvcs.poiesis.registry.ModRecipeTypes;

import java.util.Optional;

public final class MechanicalMachineRuntime {
    private final MachineContext owner;
    private final MachineDefinition definition;
    private final ComponentContainer components =
            new ComponentContainer();

    private final InventoryComponent inventory;
    private final ModuleComponent modules;
    private final EnergyComponent energy;
    private final ProcessComponent process;
    private final SideConfigurationComponent sideConfiguration;
    private final AutoIoComponent autoIo;
    private final MachineCapabilityRouter capabilityRouter;

    private final BatterySlotComponent battery;

    public MechanicalMachineRuntime(
            MachineContext owner,
            MachineDefinition definition
    ) {
        this.owner = owner;
        this.definition = definition;

        Runnable changed = owner::markPersistentDirty;

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
                        () -> owner.level() == null
                                ? 0L
                                : owner.level().getGameTime()
                )
        );

        battery = definition.hasBatterySlot()
                ? components.add(
                BatterySlotComponent.class,
                new BatterySlotComponent(
                        definition.battery(),
                        energy,
                        changed
                )
        )
                : null;

        sideConfiguration = definition.io().isEmpty()
                ? null
                : components.add(
                        SideConfigurationComponent.class,
                        new SideConfigurationComponent(
                                definition.io(),
                                changed
                        )
                );

        autoIo = definition.io().isEmpty()
                ? null
                : components.add(
                        AutoIoComponent.class,
                        new AutoIoComponent(
                                definition.io(),
                                changed
                        )
                );

        capabilityRouter = sideConfiguration == null
                ? null
                : new MachineCapabilityRouter(
                        owner,
                        definition,
                        inventory,
                        energy,
                        sideConfiguration
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
        if (owner.level() == null) {
            return Optional.empty();
        }

        SimpleContainer container =
                new SimpleContainer(
                        inventory.items()
                                .getStackInSlot(0)
                );

        return owner.level()
                .getRecipeManager()
                .getRecipeFor(
                        ModRecipeTypes
                                .MECHANICAL_PROCESSING
                                .get(),
                        container,
                        owner.level()
                );
    }

    public void tickServer() {
        boolean transferTick = shouldRunAutoIo();

        if (transferTick) {
            runAutoInputs();
        }

        if (battery != null) {
            battery.tick();
        }

        process.tick(
                owner::markPersistentDirty,
                owner::setWorking
        );

        if (transferTick) {
            runAutoOutputs();
        }
    }

    public Optional<BatterySlotComponent> battery() {
        return Optional.ofNullable(battery);
    }

    private boolean shouldRunAutoIo() {
        return owner.level() != null
                && !owner.level().isClientSide
                && owner.level().getGameTime() % 5L == 0L;
    }

    private void runAutoInputs() {
        if (autoIo == null || sideConfiguration == null) {
            return;
        }

        if (autoIo.enabled(IoChannel.ITEM, AutoIoDirection.INPUT)) {
            ItemAutoTransfer.pullInputs(
                    owner,
                    definition,
                    inventory,
                    sideConfiguration
            );
        }

        if (autoIo.enabled(IoChannel.ENERGY, AutoIoDirection.INPUT)) {
            EnergyAutoTransfer.pullEnergy(
                    owner,
                    energy,
                    sideConfiguration
            );
        }
    }

    private void runAutoOutputs() {
        if (autoIo == null || sideConfiguration == null) {
            return;
        }

        if (autoIo.enabled(IoChannel.ITEM, AutoIoDirection.OUTPUT)) {
            ItemAutoTransfer.pushOutputs(
                    owner,
                    definition,
                    inventory,
                    sideConfiguration
            );
        }
    }

    public void save(CompoundTag tag) {
        tag.putInt("machine_data_version", 4);
        components.save(tag);
    }

    public void load(CompoundTag tag) {
        components.load(tag);
    }

    public void invalidate() {
        if (capabilityRouter != null) {
            capabilityRouter.invalidate();
        }

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

    public java.util.Optional<SideConfigurationComponent> sideConfiguration() {
        return java.util.Optional.ofNullable(sideConfiguration);
    }

    public java.util.Optional<AutoIoComponent> autoIo() {
        return java.util.Optional.ofNullable(autoIo);
    }

    public java.util.Optional<MachineCapabilityRouter> capabilityRouter() {
        return java.util.Optional.ofNullable(capabilityRouter);
    }

    public void refreshIoCapabilities() {
        if (capabilityRouter != null) {
            capabilityRouter.invalidate();
        }

        owner.markSyncDirty();
    }
}
