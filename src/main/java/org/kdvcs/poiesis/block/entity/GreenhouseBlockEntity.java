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
import org.kdvcs.poiesis.machine.greenhouse.GreenhouseRuntime;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.MachineIoSpec;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideIoMode;
import org.kdvcs.poiesis.machine.io.fluid.GreenhouseRoutedFluidHandler;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.screen.GreenhouseMenu;
import org.kdvcs.poiesis.screen.menu.MenuDataCodec;

import java.util.EnumMap;
import java.util.function.BooleanSupplier;

public final class GreenhouseBlockEntity extends AbstractMachineBlockEntity<GreenhouseRuntime> {
    public static final int FLUID_CAPACITY = GreenhouseRuntime.FLUID_CAPACITY;
    public static final MachineDefinition DEFINITION = new MachineDefinition(
            "greenhouse",
            new EnergySpec(256_000, 1_280, 80, 80, 640, 80),
            5,
            2,
            3,
            MachineIoSpec.builder().item().fluid().energyInput().build(),
            new BatterySpec(800)
    );

    private final EnumMap<Direction, LazyOptional<IFluidHandler>> fluidCapabilities =
            new EnumMap<>(Direction.class);
    private LazyOptional<IFluidHandler> unsidedFluidCapability = LazyOptional.empty();

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            var process = runtime().process();
            var water = runtime().water();
            var nutrient = runtime().nutrientFluid();
            var output = runtime().outputFluid();
            return switch (index) {
                case 0 -> process.progress();
                case 1 -> process.maxProgress();
                case 2 -> runtime().energy().stored();
                case 3 -> runtime().energy().capacity();
                case 4 -> process.activeEnergyPerTick();
                case 5 -> process.state().ordinal();
                case 6 -> water.amount();
                case 7 -> water.capacity();
                case 8 -> fluidId(water.fluid());
                case 9 -> nutrient.amount();
                case 10 -> nutrient.capacity();
                case 11 -> fluidId(nutrient.fluid());
                case 12 -> output.amount();
                case 13 -> output.capacity();
                case 14 -> fluidId(output.fluid());
                case 15 -> MenuDataCodec.highWord(runtime().energy().stored());
                case 16 -> MenuDataCodec.highWord(runtime().energy().capacity());
                case 17 -> MenuDataCodec.highWord(water.amount());
                case 18 -> MenuDataCodec.highWord(water.capacity());
                case 19 -> MenuDataCodec.highWord(nutrient.amount());
                case 20 -> MenuDataCodec.highWord(nutrient.capacity());
                case 21 -> MenuDataCodec.highWord(output.amount());
                case 22 -> MenuDataCodec.highWord(output.capacity());
                default -> 0;
            };
        }

        @Override public void set(int index, int value) { }
        @Override public int getCount() { return GreenhouseMenu.DATA_COUNT; }
    };

    public GreenhouseBlockEntity(BlockPos pos, BlockState state) {
        super(
                ModBlockEntities.GREENHOUSE.get(),
                pos,
                state,
                DEFINITION,
                owner -> new GreenhouseRuntime(owner, DEFINITION, ModRecipeTypes.GREENHOUSE.get())
        );
    }

    private static int fluidId(FluidStack stack) {
        return BuiltInRegistries.FLUID.getId(stack.isEmpty() ? Fluids.EMPTY : stack.getFluid());
    }

    public FluidTank waterTank() { return runtime().water().tank(); }
    public FluidTank nutrientTank() { return runtime().nutrientFluid().tank(); }
    public FluidTank outputTank() { return runtime().outputFluid().tank(); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GreenhouseMenu(id, inventory, this, data);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (side == null) {
                if (!unsidedFluidCapability.isPresent()) {
                    unsidedFluidCapability = LazyOptional.of(() -> fluidHandler(() -> true, () -> true));
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
                            () -> configuration.allowsInput(
                                    IoChannel.FLUID,
                                    RelativeSide.fromWorldSide(worldSide, facing())
                            ),
                            () -> configuration.allowsOutput(
                                    IoChannel.FLUID,
                                    RelativeSide.fromWorldSide(worldSide, facing())
                            )
                    )
            )).cast();
        }
        return super.getCapability(capability, side);
    }

    private IFluidHandler fluidHandler(BooleanSupplier input, BooleanSupplier output) {
        return new GreenhouseRoutedFluidHandler(
                waterTank(), nutrientTank(), outputTank(), input, output
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
