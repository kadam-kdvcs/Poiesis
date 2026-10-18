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
import org.kdvcs.poiesis.machine.BiomassGasifierRuntime;
import org.kdvcs.poiesis.machine.definition.EnergySpec;
import org.kdvcs.poiesis.machine.definition.FluidSpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.MachineIoSpec;
import org.kdvcs.poiesis.machine.process.GasifierProcess;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModSounds;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.screen.BiomassGasifierMenu;
import org.kdvcs.poiesis.screen.menu.MenuDataCodec;

public final class BiomassGasifierBlockEntity
        extends AbstractMachineBlockEntity<BiomassGasifierRuntime> {
    public static final int GAS_CAPACITY = 64_000;

    /*
     * MachineRuntime currently owns an EnergyComponent for every machine.
     * This inert one-point spec is never exposed, filled or consumed because
     * the gasifier has no energy I/O channel and no battery slot.
     */
    private static final EnergySpec INERT_ENERGY =
            new EnergySpec(1, 1, 1, 1, 1, 1);

    public static final MachineDefinition DEFINITION = new MachineDefinition(
            "biomass_gasifier",
            INERT_ENERGY,
            1,
            1,
            0,
            MachineIoSpec.builder()
                    .item()
                    .fluidOutput()
                    .build(),
            null,
            new FluidSpec(GAS_CAPACITY)
    );

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            var process = runtime().process();
            var gas = runtime().fluid().orElseThrow();

            return switch (index) {
                case 0 -> process.progress();
                case 1 -> process.maxProgress();
                case 2 -> process.temperature();
                case 3 -> GasifierProcess.MAX_TEMPERATURE;
                case 4 -> gas.amount();
                case 5 -> gas.capacity();
                case 6 -> BuiltInRegistries.FLUID.getId(
                        gas.fluid().isEmpty()
                                ? Fluids.EMPTY
                                : gas.fluid().getFluid()
                );
                case 7 -> process.state().ordinal();
                case 8 -> MenuDataCodec.highWord(gas.amount());
                case 9 -> MenuDataCodec.highWord(gas.capacity());
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return BiomassGasifierMenu.DATA_COUNT;
        }
    };

    public BiomassGasifierBlockEntity(BlockPos pos, BlockState state) {
        super(
                ModBlockEntities.BIOMASS_GASIFIER.get(),
                pos,
                state,
                DEFINITION,
                owner -> new BiomassGasifierRuntime(
                        owner,
                        DEFINITION,
                        ModRecipeTypes.GASIFICATION.get()
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
        return new BiomassGasifierMenu(id, inventory, this, data);
    }

    @Override
    protected SoundEvent workingSound() {
        return ModSounds.BIOMASS_GASIFIER_WORKING.get();
    }
}
