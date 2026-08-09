package org.kdvcs.poiesis.machine.fabrication;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.machine.api.ComponentContainer;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.io.AutoIoComponent;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.MachineCapabilityRouter;
import org.kdvcs.poiesis.machine.io.SideConfigurationComponent;
import org.kdvcs.poiesis.machine.io.transfer.EnergyAutoTransfer;
import org.kdvcs.poiesis.machine.io.transfer.ItemAutoTransfer;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;
import org.kdvcs.poiesis.registry.ModItems;
import org.kdvcs.poiesis.registry.ModRecipeTypes;

import java.util.Optional;

public final class ModuleFabricatorRuntime {

    private final MachineContext owner;
    private final MachineDefinition definition;
    private final ComponentContainer components =
            new ComponentContainer();

    private final InventoryComponent inventory;
    private final EnergyComponent energy;
    private final BatterySlotComponent battery;

    private final SideConfigurationComponent sideConfiguration;
    private final AutoIoComponent autoIo;
    private final MachineCapabilityRouter capabilityRouter;

    private final ModuleFabricationProcess process;

    public ModuleFabricatorRuntime(
            MachineContext owner,
            MachineDefinition definition
    ) {
        this.owner = owner;
        this.definition = definition;

        Runnable changed =
                owner::markPersistentDirty;

        inventory = components.add(
                InventoryComponent.class,
                new InventoryComponent(
                        definition.inputSlots()
                                + definition.outputSlots(),
                        slot ->
                                slot >= definition.inputSlots(),
                        this::isValidInput,
                        changed
                )
        );

        energy = components.add(
                EnergyComponent.class,
                new EnergyComponent(
                        definition.energy(),
                        changed,
                        () ->
                                owner.level() == null
                                        ? 0L
                                        : owner.level()
                                        .getGameTime()
                )
        );

        battery =
                definition.hasBatterySlot()
                        ? components.add(
                        BatterySlotComponent.class,
                        new BatterySlotComponent(
                                definition.battery(),
                                energy,
                                changed
                        )
                )
                        : null;

        sideConfiguration = components.add(
                SideConfigurationComponent.class,
                new SideConfigurationComponent(
                        definition.io(),
                        changed
                )
        );

        autoIo = components.add(
                AutoIoComponent.class,
                new AutoIoComponent(
                        definition.io(),
                        changed
                )
        );

        capabilityRouter =
                new MachineCapabilityRouter(
                        owner,
                        definition,
                        inventory,
                        energy,
                        sideConfiguration
                );

        process = components.add(
                ModuleFabricationProcess.class,
                new ModuleFabricationProcess(
                        inventory,
                        energy,
                        this::findRecipeById
                )
        );
    }

    private boolean isValidInput(
            int slot,
            ItemStack stack
    ) {
        if (slot
                == ModuleFabricationProcess.MODULE_BASE_SLOT) {

            return stack.is(
                    ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get()
            ) || stack.is(
                    ModItems.PERFORMANCE_MODULE_SUBSTRATE.get()
            );
        }

        if (slot
                >= ModuleFabricationProcess.FIRST_MATERIAL_SLOT
                && slot
                < ModuleFabricationProcess.MATERIAL_SLOT_END) {

            return !stack.is(
                    ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get()
            ) && !stack.is(
                    ModItems.PERFORMANCE_MODULE_SUBSTRATE.get()
            );
        }

        return false;
    }

    public Optional<ModuleFabricationRecipe> findRecipeById(
            ResourceLocation id
    ) {
        if (owner.level() == null
                || id == null) {

            return Optional.empty();
        }

        return owner.level()
                .getRecipeManager()
                .byKey(id)
                .filter(
                        recipe ->
                                recipe.getType()
                                        == ModRecipeTypes.MODULE_FABRICATION.get()
                )
                .map(
                        recipe ->
                                (ModuleFabricationRecipe) recipe
                );
    }

    public void tickServer() {
        boolean transferTick =
                owner.level() != null
                        && !owner.level().isClientSide
                        && owner.level()
                        .getGameTime() % 5L == 0L;

        if (transferTick) {
            if (autoIo.enabled(
                    IoChannel.ITEM,
                    AutoIoDirection.INPUT
            )) {
                ItemAutoTransfer.pullInputs(
                        owner,
                        definition,
                        inventory,
                        sideConfiguration
                );
            }

            if (autoIo.enabled(
                    IoChannel.ENERGY,
                    AutoIoDirection.INPUT
            )) {
                EnergyAutoTransfer.pullEnergy(
                        owner,
                        energy,
                        sideConfiguration
                );
            }
        }

        if (battery != null) {
            battery.tick();
        }

        process.tick(
                owner::markPersistentDirty,
                owner::setWorking
        );

        if (transferTick
                && autoIo.enabled(
                IoChannel.ITEM,
                AutoIoDirection.OUTPUT
        )) {
            ItemAutoTransfer.pushOutputs(
                    owner,
                    definition,
                    inventory,
                    sideConfiguration
            );
        }
    }

    public void save(
            CompoundTag tag
    ) {
        components.save(tag);
    }

    public void load(
            CompoundTag tag
    ) {
        components.load(tag);
    }

    public void invalidate() {
        capabilityRouter.invalidate();
        components.invalidate();
    }

    public void refreshIoCapabilities() {
        capabilityRouter.invalidate();
        owner.markSyncDirty();
    }

    public InventoryComponent inventory() {
        return inventory;
    }

    public EnergyComponent energy() {
        return energy;
    }

    public Optional<BatterySlotComponent> battery() {
        return Optional.ofNullable(battery);
    }

    public ModuleFabricationProcess process() {
        return process;
    }

    public Optional<SideConfigurationComponent> sideConfiguration() {
        return Optional.of(sideConfiguration);
    }

    public Optional<AutoIoComponent> autoIo() {
        return Optional.of(autoIo);
    }

    public MachineCapabilityRouter capabilityRouter() {
        return capabilityRouter;
    }
}