package org.kdvcs.poiesis.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;
import org.kdvcs.poiesis.block.entity.SeparatorBlockEntity;
import org.kdvcs.poiesis.registry.ModBlockEntities;

public final class SeparatorBlock extends AbstractMachineBlock<SeparatorBlockEntity> {
    public SeparatorBlock() {
        super(8);
    }

    @Override
    protected SeparatorBlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new SeparatorBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<SeparatorBlockEntity> machineBlockEntityType() {
        return ModBlockEntities.SEPARATOR.get();
    }
}
