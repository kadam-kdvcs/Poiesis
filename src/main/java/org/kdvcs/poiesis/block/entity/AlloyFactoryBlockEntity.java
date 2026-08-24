package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.entity.base.AbstractMachineBlockEntity;
import org.kdvcs.poiesis.machine.alloy.AlloyFactoryRuntime;
import org.kdvcs.poiesis.machine.definition.BatterySpec;
import org.kdvcs.poiesis.machine.definition.EnergySpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.MachineIoSpec;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModSounds;
import org.kdvcs.poiesis.screen.AlloyFactoryMenu;

public final class AlloyFactoryBlockEntity extends AbstractMachineBlockEntity<AlloyFactoryRuntime> {
    public static final MachineDefinition DEFINITION = new MachineDefinition(
            "alloy_factory",
            new EnergySpec(256_000, 1_280, 80, 80, 640, 80),
            18,
            6,
            3,
            MachineIoSpec.builder().item().energyInput().build(),
            new BatterySpec(800)
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
                case 5 -> runtime().process().autoCrafting() ? 1 : 0;
                case 6 -> runtime().process().isProcessing() ? 1 : 0;
                default -> 0;
            };
        }

        @Override public void set(int index, int value) { }
        @Override public int getCount() { return 7; }
    };

    public AlloyFactoryBlockEntity(BlockPos pos, BlockState state) {
        super(
                ModBlockEntities.ALLOY_FACTORY.get(),
                pos,
                state,
                DEFINITION,
                owner -> new AlloyFactoryRuntime(owner, DEFINITION)
        );
    }

    @Override
    protected SoundEvent workingSound() {
        return ModSounds.ALLOY_FACTORY_WORKING.get();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AlloyFactoryMenu(
                id, inventory, this, data, runtime().process().selectedRecipeId()
        );
    }
}
