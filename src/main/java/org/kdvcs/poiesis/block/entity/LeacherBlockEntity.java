package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
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
import org.kdvcs.poiesis.machine.LeacherRuntime;
import org.kdvcs.poiesis.machine.definition.BatterySpec;
import org.kdvcs.poiesis.machine.definition.EnergySpec;
import org.kdvcs.poiesis.machine.definition.FluidSpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.MachineIoSpec;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideIoMode;
import org.kdvcs.poiesis.machine.io.fluid.DualTankRoutedFluidHandler;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.registry.ModSounds;
import org.kdvcs.poiesis.screen.LeacherMenu;
import org.kdvcs.poiesis.screen.menu.MenuDataCodec;

import java.util.EnumMap;
import java.util.function.BooleanSupplier;

public final class LeacherBlockEntity extends AbstractMachineBlockEntity<LeacherRuntime> {
    public static final int FLUID_CAPACITY = 64_000;
    public static final MachineDefinition DEFINITION = new MachineDefinition(
            "leacher",
            new EnergySpec(64_000, 640, 20, 40, 320, 20),
            1,
            1,
            3,
            MachineIoSpec.builder().item().fluid().energyInput().build(),
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
            var input = runtime().fluid().orElseThrow();
            var output = runtime().outputFluid();
            return switch (index) {
                case 0 -> process.progress();
                case 1 -> process.maxProgress();
                case 2 -> runtime().energy().stored();
                case 3 -> runtime().energy().capacity();
                case 4 -> process.activeEnergyPerTick();
                case 5 -> process.activeVoltage();
                case 6 -> process.state().ordinal();
                case 7 -> input.amount();
                case 8 -> input.capacity();
                case 9 -> fluidId(input.fluid());
                case 10 -> output.amount();
                case 11 -> output.capacity();
                case 12 -> fluidId(output.fluid());
                case 13 -> MenuDataCodec.highWord(runtime().energy().stored());
                case 14 -> MenuDataCodec.highWord(runtime().energy().capacity());
                case 15 -> MenuDataCodec.highWord(input.amount());
                case 16 -> MenuDataCodec.highWord(input.capacity());
                case 17 -> MenuDataCodec.highWord(output.amount());
                case 18 -> MenuDataCodec.highWord(output.capacity());
                default -> 0;
            };
        }

        @Override public void set(int index, int value) { }
        @Override public int getCount() { return LeacherMenu.DATA_COUNT; }
    };

    public LeacherBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEACHER.get(), pos, state, DEFINITION,
                owner -> new LeacherRuntime(owner, DEFINITION, ModRecipeTypes.LEACHING.get()));
    }

    private static int fluidId(FluidStack stack) {
        return BuiltInRegistries.FLUID.getId(stack.isEmpty() ? Fluids.EMPTY : stack.getFluid());
    }

    public FluidTank inputTank() { return runtime().fluid().orElseThrow().tank(); }
    public FluidTank outputTank() { return runtime().outputFluid().tank(); }

    @Override
    protected SoundEvent workingSound() {
        return ModSounds.LEACHER_WORKING.get();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new LeacherMenu(id, inventory, this, data);
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
            if (configuration.mode(IoChannel.FLUID, relative)
                    == SideIoMode.NONE) {
                return LazyOptional.empty();
            }
            return fluidCapabilities.computeIfAbsent(side, worldSide -> LazyOptional.of(() ->
                    fluidHandler(
                            () -> configuration.allowsInput(IoChannel.FLUID,
                                    RelativeSide.fromWorldSide(worldSide, facing())),
                            () -> configuration.allowsOutput(IoChannel.FLUID,
                                    RelativeSide.fromWorldSide(worldSide, facing()))
                    ))).cast();
        }
        return super.getCapability(capability, side);
    }

    private IFluidHandler fluidHandler(
            BooleanSupplier input,
            BooleanSupplier output
    ) {
        return new DualTankRoutedFluidHandler(inputTank(), outputTank(), input, output);
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
