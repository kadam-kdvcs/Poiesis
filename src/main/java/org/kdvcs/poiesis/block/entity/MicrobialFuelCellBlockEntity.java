package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.entity.base.AbstractMachineBlockEntity;
import org.kdvcs.poiesis.machine.definition.BatterySpec;
import org.kdvcs.poiesis.machine.definition.EnergySpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.generator.GeneratorDefinition;
import org.kdvcs.poiesis.machine.generator.MicrobialFuelCellRuntime;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.MachineIoSpec;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideIoMode;
import org.kdvcs.poiesis.machine.io.fluid.MfcRoutedFluidHandler;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.screen.MicrobialFuelCellMenu;
import org.kdvcs.poiesis.screen.menu.MenuDataCodec;

import java.util.EnumMap;

public final class MicrobialFuelCellBlockEntity
        extends AbstractMachineBlockEntity<MicrobialFuelCellRuntime> {
    public static final GeneratorDefinition GENERATOR_DEFINITION = new GeneratorDefinition(
            "microbial_fuel_cell",
            256_000,
            320,
            120,
            MicrobialFuelCellRuntime.TANK_CAPACITY,
            640,
            3
    );

    public static final MachineDefinition DEFINITION = new MachineDefinition(
            "microbial_fuel_cell",
            new EnergySpec(256_000, 640, 320, 120, 120, 120, 1),
            0,
            0,
            3,
            MachineIoSpec.builder().mfcFluidInput().energyOutput().build(),
            new BatterySpec(640)
    );

    private final EnumMap<Direction, LazyOptional<IFluidHandler>> fluidCapabilities =
            new EnumMap<>(Direction.class);
    private LazyOptional<IFluidHandler> unsidedFluidCapability = LazyOptional.empty();

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            var runtime = runtime();
            var process = runtime.process();
            var anolyte = runtime.anolyte();
            var catholyte = runtime.catholyte();
            return switch (index) {
                case 0 -> process.progress();
                case 1 -> process.maxProgress();
                case 2 -> runtime.energy().stored();
                case 3 -> runtime.energy().capacity();
                case 4 -> process.activeGenerationPerTick();
                case 5 -> runtime.outputPower();
                case 6 -> anolyte.amount();
                case 7 -> anolyte.capacity();
                case 8 -> fluidId(anolyte.fluid());
                case 9 -> catholyte.amount();
                case 10 -> catholyte.capacity();
                case 11 -> fluidId(catholyte.fluid());
                case 12 -> process.state().ordinal();
                case 13 -> MenuDataCodec.highWord(process.progress());
                case 14 -> MenuDataCodec.highWord(process.maxProgress());
                case 15 -> MenuDataCodec.highWord(runtime.energy().stored());
                case 16 -> MenuDataCodec.highWord(runtime.energy().capacity());
                case 17 -> MenuDataCodec.highWord(anolyte.amount());
                case 18 -> MenuDataCodec.highWord(anolyte.capacity());
                case 19 -> MenuDataCodec.highWord(catholyte.amount());
                case 20 -> MenuDataCodec.highWord(catholyte.capacity());
                default -> 0;
            };
        }

        @Override public void set(int index, int value) { }
        @Override public int getCount() { return MicrobialFuelCellMenu.DATA_COUNT; }
    };

    public MicrobialFuelCellBlockEntity(BlockPos pos, BlockState state) {
        super(
                ModBlockEntities.MICROBIAL_FUEL_CELL.get(),
                pos,
                state,
                DEFINITION,
                owner -> new MicrobialFuelCellRuntime(
                        owner,
                        GENERATOR_DEFINITION,
                        DEFINITION,
                        ModRecipeTypes.MICROBIAL_FUEL_CELL.get()
                )
        );
    }

    private static int fluidId(FluidStack stack) {
        return BuiltInRegistries.FLUID.getId(
                stack.isEmpty() ? Fluids.EMPTY : stack.getFluid()
        );
    }

    public FluidTank anolyteTank() { return runtime().anolyte().tank(); }
    public FluidTank catholyteTank() { return runtime().catholyte().tank(); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MicrobialFuelCellMenu(id, inventory, this, data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (side == null) {
                if (!unsidedFluidCapability.isPresent()) {
                    unsidedFluidCapability = LazyOptional.of(
                            () -> fluidHandler(MfcRoutedFluidHandler.Chamber.ALL)
                    );
                }
                return unsidedFluidCapability.cast();
            }

            RelativeSide relative = RelativeSide.fromWorldSide(side, facing());
            var configuration = runtime().sideConfiguration().orElseThrow();
            SideIoMode mode = configuration.mode(IoChannel.FLUID, relative);
            if (mode == SideIoMode.NONE) {
                return LazyOptional.empty();
            }

            MfcRoutedFluidHandler.Chamber chamber = switch (mode) {
                case INPUT_ANODE -> MfcRoutedFluidHandler.Chamber.ANOLYTE;
                case INPUT_CATHODE -> MfcRoutedFluidHandler.Chamber.CATHOLYTE;
                default -> MfcRoutedFluidHandler.Chamber.ALL;
            };

            return fluidCapabilities.computeIfAbsent(side, worldSide -> LazyOptional.of(() ->
                    fluidHandler(chamber)
            )).cast();
        }
        return super.getCapability(capability, side);
    }

    private IFluidHandler fluidHandler(MfcRoutedFluidHandler.Chamber chamber) {
        return new MfcRoutedFluidHandler(
                anolyteTank(),
                catholyteTank(),
                () -> true,
                chamber
        );
    }

    @Override
    protected void onIoCapabilitiesRefreshed() {
        fluidCapabilities.values().forEach(LazyOptional::invalidate);
        fluidCapabilities.clear();
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
