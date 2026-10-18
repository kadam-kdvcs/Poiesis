package org.kdvcs.poiesis.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;
import org.kdvcs.poiesis.block.entity.GasGeneratorBlockEntity;
import org.kdvcs.poiesis.registry.ModBlockEntities;

public final class GasGeneratorBlock extends AbstractMachineBlock<GasGeneratorBlockEntity> {
    public GasGeneratorBlock() {
        super(8);
    }

    @Override
    protected GasGeneratorBlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new GasGeneratorBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<GasGeneratorBlockEntity> machineBlockEntityType() {
        return ModBlockEntities.GAS_GENERATOR.get();
    }
}
