package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.entity.base.AbstractMachineBlockEntity;
import org.kdvcs.poiesis.machine.MaterialMachineRuntime;
import org.kdvcs.poiesis.machine.definition.BatterySpec;
import org.kdvcs.poiesis.machine.definition.EnergySpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.MachineIoSpec;
import org.kdvcs.poiesis.machine.module.ModuleLayout;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipe;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.screen.MechanicalProcessorMenu;

public final class MechanicalProcessorBlockEntity
        extends AbstractMachineBlockEntity<MaterialMachineRuntime<MechanicalProcessingRecipe>> {

    public static final MachineDefinition DEFINITION = new MachineDefinition(
            "mechanical_processor",
            new EnergySpec(40_000, 640, 20, 40, 320, 20),
            1,
            1,
            3,
            MachineIoSpec.builder().item().energyInput().build(),
            new BatterySpec(400)
    );

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> runtime().process().progress();
                case 1 -> runtime().process().maxProgress();
                case 2 -> runtime().energy().stored();
                case 3 -> runtime().energy().capacity();
                case 4 -> runtime().process().activeEnergyPerTick();
                case 5 -> runtime().process().activeVoltage();
                case 6 -> runtime().process().state().ordinal();
                case 7 -> runtime().modules().currentMode().ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 8;
        }
    };

    public MechanicalProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(
                ModBlockEntities.MECHANICAL_PROCESSOR.get(),
                pos,
                state,
                DEFINITION,
                owner -> new MaterialMachineRuntime<>(
                        owner,
                        DEFINITION,
                        ModRecipeTypes.MECHANICAL_PROCESSING.get(),
                        ModuleLayout.mechanicalProcessing()
                )
        );
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(
            int id,
            Inventory inventory,
            Player player
    ) {
        return new MechanicalProcessorMenu(id, inventory, this, data);
    }
}
