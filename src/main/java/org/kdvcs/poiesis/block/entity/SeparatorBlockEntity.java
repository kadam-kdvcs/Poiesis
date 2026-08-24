package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.entity.base.AbstractMachineBlockEntity;
import org.kdvcs.poiesis.machine.SeparatorRuntime;
import org.kdvcs.poiesis.machine.definition.BatterySpec;
import org.kdvcs.poiesis.machine.definition.EnergySpec;
import org.kdvcs.poiesis.machine.definition.FluidSpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.MachineIoSpec;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModItems;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.registry.ModSounds;
import org.kdvcs.poiesis.screen.SeparatorMenu;

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
                    .fluidInput()
                    .energyInput()
                    .build(),
            new BatterySpec(400),
            new FluidSpec(FLUID_CAPACITY)
    );

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
                        ModItems.WET_SEPARATION
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
}
