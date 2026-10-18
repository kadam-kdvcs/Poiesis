package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.entity.base.AbstractMachineBlockEntity;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.generator.GasGeneratorRuntime;
import org.kdvcs.poiesis.machine.generator.GeneratorDefinition;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.screen.GasGeneratorMenu;
import org.kdvcs.poiesis.screen.menu.MenuDataCodec;

public final class GasGeneratorBlockEntity
        extends AbstractMachineBlockEntity<GasGeneratorRuntime> {
    public static final GeneratorDefinition GENERATOR_DEFINITION =
            new GeneratorDefinition(
                    "gas_generator",
                    128_000,
                    160,
                    80,
                    64_000,
                    640,
                    3
            );

    public static final MachineDefinition DEFINITION =
            GENERATOR_DEFINITION.machineDefinition();

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            var runtime = runtime();
            var process = runtime.process();
            var fuel = runtime.fluid().orElseThrow();
            return switch (index) {
                case 0 -> process.burnRemaining();
                case 1 -> process.burnTotal();
                case 2 -> runtime.energy().stored();
                case 3 -> runtime.energy().capacity();
                case 4 -> process.activeGenerationPerTick();
                case 5 -> runtime.outputPower();
                case 6 -> fuel.amount();
                case 7 -> fuel.capacity();
                case 8 -> BuiltInRegistries.FLUID.getId(
                        fuel.fluid().isEmpty() ? Fluids.EMPTY : fuel.fluid().getFluid()
                );
                case 9 -> process.state().ordinal();
                case 10 -> MenuDataCodec.highWord(process.burnRemaining());
                case 11 -> MenuDataCodec.highWord(process.burnTotal());
                case 12 -> MenuDataCodec.highWord(runtime.energy().stored());
                case 13 -> MenuDataCodec.highWord(runtime.energy().capacity());
                case 14 -> MenuDataCodec.highWord(fuel.amount());
                case 15 -> MenuDataCodec.highWord(fuel.capacity());
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return GasGeneratorMenu.DATA_COUNT;
        }
    };

    public GasGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(
                ModBlockEntities.GAS_GENERATOR.get(),
                pos,
                state,
                DEFINITION,
                owner -> new GasGeneratorRuntime(
                        owner,
                        GENERATOR_DEFINITION,
                        DEFINITION,
                        ModRecipeTypes.GAS_GENERATION.get()
                )
        );
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new GasGeneratorMenu(id, inventory, this, data);
    }
}
