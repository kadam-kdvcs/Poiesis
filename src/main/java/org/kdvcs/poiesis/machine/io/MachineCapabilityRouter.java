package org.kdvcs.poiesis.machine.io;

import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.fluid.FluidComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.io.energy.RoutedEnergyStorage;
import org.kdvcs.poiesis.machine.io.item.RoutedItemHandler;
import org.kdvcs.poiesis.machine.io.fluid.RoutedFluidHandler;

import java.util.EnumMap;
import java.util.function.BooleanSupplier;

public final class MachineCapabilityRouter {

    private final MachineContext owner;
    private final MachineDefinition definition;
    private final InventoryComponent inventory;
    private final EnergyComponent energy;
    private final FluidComponent fluid;
    private final SideConfigurationComponent configuration;
    private final BooleanSupplier fluidEnabled;

    private final EnumMap<Direction, LazyOptional<IItemHandler>> itemCapabilities =
            new EnumMap<>(Direction.class);

    private final EnumMap<Direction, LazyOptional<IEnergyStorage>> energyCapabilities =
            new EnumMap<>(Direction.class);

    private final EnumMap<Direction, LazyOptional<IFluidHandler>> fluidCapabilities =
            new EnumMap<>(Direction.class);
    private LazyOptional<IFluidHandler> unsidedFluidCapability = LazyOptional.empty();

    public MachineCapabilityRouter(
            MachineContext owner,
            MachineDefinition definition,
            InventoryComponent inventory,
            EnergyComponent energy,
            FluidComponent fluid,
            SideConfigurationComponent configuration,
            BooleanSupplier fluidEnabled
    ) {
        this.owner = owner;
        this.definition = definition;
        this.inventory = inventory;
        this.energy = energy;
        this.fluid = fluid;
        this.configuration = configuration;
        this.fluidEnabled = fluidEnabled;
    }

    public LazyOptional<IItemHandler> itemCapability(Direction worldSide) {
        if (!definition.io().supports(IoChannel.ITEM)) {
            return LazyOptional.empty();
        }

        if (worldSide == null) {
            return inventory.capability();
        }

        RelativeSide relativeSide =
                RelativeSide.fromWorldSide(
                        worldSide,
                        owner.facing()
                );

        if (configuration.mode(
                IoChannel.ITEM,
                relativeSide
        ) == SideIoMode.NONE) {
            return LazyOptional.empty();
        }

        return itemCapabilities.computeIfAbsent(
                worldSide,
                side -> LazyOptional.of(
                        () -> new RoutedItemHandler(
                                inventory.items(),
                                configuration,
                                () -> RelativeSide.fromWorldSide(
                                        side,
                                        owner.facing()
                                ),
                                definition.inputSlots(),
                                definition.outputSlots()
                        )
                )
        );
    }

    public LazyOptional<IEnergyStorage> energyCapability(Direction worldSide) {
        if (!definition.io().supports(IoChannel.ENERGY)) {
            return LazyOptional.empty();
        }

        if (worldSide == null) {
            return energy.capability();
        }

        RelativeSide relativeSide =
                RelativeSide.fromWorldSide(
                        worldSide,
                        owner.facing()
                );

        if (configuration.mode(
                IoChannel.ENERGY,
                relativeSide
        ) == SideIoMode.NONE) {
            return LazyOptional.empty();
        }

        return energyCapabilities.computeIfAbsent(
                worldSide,
                side -> LazyOptional.of(
                        () -> new RoutedEnergyStorage(
                                energy.view(),
                                configuration,
                                () -> RelativeSide.fromWorldSide(
                                        side,
                                        owner.facing()
                                )
                        )
                )
        );
    }

    public LazyOptional<IFluidHandler> fluidCapability(Direction worldSide) {
        if (fluid == null
                || !definition.io().supports(IoChannel.FLUID)
                || !fluidEnabled.getAsBoolean()) {
            return LazyOptional.empty();
        }

        if (worldSide == null) {
            if (!unsidedFluidCapability.isPresent()) {
                var channel = definition.io().channel(IoChannel.FLUID).orElseThrow();
                boolean allowsAnyInput = channel.allowedModes().stream()
                        .anyMatch(SideIoMode::allowsInput);
                boolean allowsAnyOutput = channel.allowedModes().stream()
                        .anyMatch(SideIoMode::allowsOutput);
                unsidedFluidCapability = LazyOptional.of(() -> new RoutedFluidHandler(
                        fluid.tank(),
                        fluidEnabled,
                        () -> allowsAnyInput,
                        () -> allowsAnyOutput
                ));
            }
            return unsidedFluidCapability;
        }

        RelativeSide relativeSide = RelativeSide.fromWorldSide(
                worldSide,
                owner.facing()
        );

        if (configuration.mode(IoChannel.FLUID, relativeSide) == SideIoMode.NONE) {
            return LazyOptional.empty();
        }

        return fluidCapabilities.computeIfAbsent(
                worldSide,
                side -> LazyOptional.of(() -> new RoutedFluidHandler(
                        fluid.tank(),
                        fluidEnabled,
                        () -> configuration.allowsInput(
                                IoChannel.FLUID,
                                RelativeSide.fromWorldSide(side, owner.facing())
                        ),
                        () -> configuration.allowsOutput(
                                IoChannel.FLUID,
                                RelativeSide.fromWorldSide(side, owner.facing())
                        )
                ))
        );
    }

    public void invalidate() {
        itemCapabilities.values()
                .forEach(
                        LazyOptional::invalidate
                );

        itemCapabilities.clear();

        energyCapabilities.values()
                .forEach(
                        LazyOptional::invalidate
                );

        energyCapabilities.clear();

        fluidCapabilities.values().forEach(LazyOptional::invalidate);
        fluidCapabilities.clear();
        unsidedFluidCapability.invalidate();
        unsidedFluidCapability = LazyOptional.empty();
    }
}
