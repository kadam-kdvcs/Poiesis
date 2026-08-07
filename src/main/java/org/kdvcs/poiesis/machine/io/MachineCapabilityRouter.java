package org.kdvcs.poiesis.machine.io;

import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.io.energy.RoutedEnergyStorage;
import org.kdvcs.poiesis.machine.io.item.RoutedItemHandler;

import java.util.EnumMap;

public final class MachineCapabilityRouter {
    private final MachineContext owner;
    private final MachineDefinition definition;
    private final InventoryComponent inventory;
    private final EnergyComponent energy;
    private final SideConfigurationComponent configuration;

    private final EnumMap<Direction, LazyOptional<IItemHandler>> itemCapabilities =
            new EnumMap<>(Direction.class);
    private final EnumMap<Direction, LazyOptional<IEnergyStorage>> energyCapabilities =
            new EnumMap<>(Direction.class);

    public MachineCapabilityRouter(
            MachineContext owner,
            MachineDefinition definition,
            InventoryComponent inventory,
            EnergyComponent energy,
            SideConfigurationComponent configuration
    ) {
        this.owner = owner;
        this.definition = definition;
        this.inventory = inventory;
        this.energy = energy;
        this.configuration = configuration;
    }

    public LazyOptional<IItemHandler> itemCapability(Direction worldSide) {
        if (!definition.io().supports(IoChannel.ITEM)) {
            return LazyOptional.empty();
        }

        if (worldSide == null) {
            return inventory.capability();
        }

        return itemCapabilities.computeIfAbsent(
                worldSide,
                side -> LazyOptional.of(() -> new RoutedItemHandler(
                        inventory.items(),
                        configuration,
                        () -> RelativeSide.fromWorldSide(side, owner.facing()),
                        definition.inputSlots(),
                        definition.outputSlots()
                ))
        );
    }

    public LazyOptional<IEnergyStorage> energyCapability(Direction worldSide) {
        if (!definition.io().supports(IoChannel.ENERGY)) {
            return LazyOptional.empty();
        }

        if (worldSide == null) {
            return energy.capability();
        }

        return energyCapabilities.computeIfAbsent(
                worldSide,
                side -> LazyOptional.of(() -> new RoutedEnergyStorage(
                        energy.view(),
                        configuration,
                        () -> RelativeSide.fromWorldSide(side, owner.facing())
                ))
        );
    }

    public void invalidate() {
        itemCapabilities.values().forEach(LazyOptional::invalidate);
        itemCapabilities.clear();

        energyCapabilities.values().forEach(LazyOptional::invalidate);
        energyCapabilities.clear();
    }
}
