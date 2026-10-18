package org.kdvcs.poiesis.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;
import org.kdvcs.poiesis.block.entity.LeacherBlockEntity;
import org.kdvcs.poiesis.registry.ModBlockEntities;

public final class LeacherBlock extends AbstractMachineBlock<LeacherBlockEntity> {
    public LeacherBlock() { super(8); }

    @Override
    protected LeacherBlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new LeacherBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<LeacherBlockEntity> machineBlockEntityType() {
        return ModBlockEntities.LEACHER.get();
    }
}
