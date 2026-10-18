package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.entity.base.AbstractMachineBlockEntity;
import org.kdvcs.poiesis.machine.SeparatorRuntime;
import org.kdvcs.poiesis.machine.definition.BatterySpec;
import org.kdvcs.poiesis.machine.definition.EnergySpec;
import org.kdvcs.poiesis.machine.definition.FluidSpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.MachineIoSpec;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideIoMode;
import org.kdvcs.poiesis.machine.io.fluid.DualTankRoutedFluidHandler;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModItems;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.registry.ModSounds;
import org.kdvcs.poiesis.screen.SeparatorMenu;
import org.kdvcs.poiesis.screen.menu.MenuDataCodec;

import java.util.EnumMap;
import java.util.function.BooleanSupplier;

public final class SeparatorBlockEntity
        extends AbstractMachineBlockEntity<SeparatorRuntime> {

    public static final int FLUID_CAPACITY = 64_000;

    public static final MachineDefinition DEFINITION = new MachineDefinition(
            "separator",
            new EnergySpec(64_000, 640, 20, 40, 320, 20),
            2,
            4,
            3,
            MachineIoSpec.builder()
                    .item()
                    .fluid()
                    .energyInput()
                    .build(),
            new BatterySpec(400),
            new FluidSpec(FLUID_CAPACITY)
    );

    private final EnumMap<Direction, LazyOptional<IFluidHandler>> fluidCapabilities =
            new EnumMap<>(Direction.class);
    private LazyOptional<IFluidHandler> unsidedFluidCapability = LazyOptional.empty();

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            var process = runtime().process();
            var fluid = runtime().fluid().orElseThrow();

            return switch (index) {
                case 0 -> process.progress();
                case 1 -> process.maxProgress();
                case 2 -> runtime().energy().stored();
                case 3 -> runtime().energy().capacity();
                case 4 -> process.activeEnergyPerTick();
                case 5 -> process.activeVoltage();
                case 6 -> process.state().ordinal();
                case 7 -> fluid.amount();
                case 8 -> fluid.capacity();
                case 9 -> BuiltInRegistries.FLUID.getId(
                        fluid.fluid().isEmpty()
                                ? Fluids.EMPTY
                                : fluid.fluid().getFluid()
                );
                case 10 -> runtime().fluidSlotEnabled() ? 1 : 0;
                case 11 -> runtime().electrodeSlotEnabled() ? 1 : 0;
                case 12 -> runtime().electrodeSlotVisible() ? 1 : 0;
                case 13 -> MenuDataCodec.highWord(runtime().energy().stored());
                case 14 -> MenuDataCodec.highWord(runtime().energy().capacity());
                case 15 -> MenuDataCodec.highWord(fluid.amount());
                case 16 -> MenuDataCodec.highWord(fluid.capacity());
                case 17 -> runtime().outputFluid().amount();
                case 18 -> runtime().outputFluid().capacity();
                case 19 -> BuiltInRegistries.FLUID.getId(
                        runtime().outputFluid().fluid().isEmpty()
                                ? Fluids.EMPTY
                                : runtime().outputFluid().fluid().getFluid()
                );
                case 20 -> MenuDataCodec.highWord(runtime().outputFluid().amount());
                case 21 -> MenuDataCodec.highWord(runtime().outputFluid().capacity());
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return SeparatorMenu.DATA_COUNT;
        }
    };

    public SeparatorBlockEntity(BlockPos pos, BlockState state) {
        super(
                ModBlockEntities.SEPARATOR.get(),
                pos,
                state,
                DEFINITION,
                owner -> new SeparatorRuntime(
                        owner,
                        DEFINITION,
                        ModRecipeTypes.SEPARATING.get(),
                        ModItems.WET_SEPARATION,
                        ModItems.RINSING,
                        ModItems.ELECTROSTATIC_SEPARATION
                )
        );
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new SeparatorMenu(id, inventory, this, data);
    }

    @Override
    protected SoundEvent workingSound() {
        return ModSounds.SEPARATOR_WORKING.get();
    }

    public FluidTank inputTank() {
        return runtime().fluid().orElseThrow().tank();
    }

    public FluidTank outputTank() {
        return runtime().outputFluid().tank();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (!runtime().fluidSlotEnabled()) {
                return LazyOptional.empty();
            }

            if (side == null) {
                if (!unsidedFluidCapability.isPresent()) {
                    unsidedFluidCapability = LazyOptional.of(() -> fluidHandler(
                            () -> runtime().fluidSlotEnabled(),
                            () -> runtime().rinsingModuleInstalled()
                    ));
                }
                return unsidedFluidCapability.cast();
            }

            RelativeSide relative = RelativeSide.fromWorldSide(side, facing());
            var configuration = runtime().sideConfiguration().orElseThrow();
            if (configuration.mode(IoChannel.FLUID, relative) == SideIoMode.NONE) {
                return LazyOptional.empty();
            }

            return fluidCapabilities.computeIfAbsent(side, worldSide -> LazyOptional.of(() ->
                    fluidHandler(
                            () -> runtime().fluidSlotEnabled()
                                    && configuration.allowsInput(
                                    IoChannel.FLUID,
                                    RelativeSide.fromWorldSide(worldSide, facing())
                            ),
                            () -> runtime().rinsingModuleInstalled()
                                    && configuration.allowsOutput(
                                    IoChannel.FLUID,
                                    RelativeSide.fromWorldSide(worldSide, facing())
                            )
                    ))).cast();
        }
        return super.getCapability(capability, side);
    }

    private IFluidHandler fluidHandler(
            BooleanSupplier input,
            BooleanSupplier output
    ) {
        return new DualTankRoutedFluidHandler(
                inputTank(),
                outputTank(),
                input,
                output
        );
    }

    @Override
    public void invalidateCaps() {
        fluidCapabilities.values().forEach(LazyOptional::invalidate);
        fluidCapabilities.clear();
        unsidedFluidCapability.invalidate();
        unsidedFluidCapability = LazyOptional.empty();
        super.invalidateCaps();
    }
}
