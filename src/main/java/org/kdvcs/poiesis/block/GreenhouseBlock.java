package org.kdvcs.poiesis.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;
import org.kdvcs.poiesis.block.entity.GreenhouseBlockEntity;
import org.kdvcs.poiesis.registry.ModBlockEntities;

public final class GreenhouseBlock extends AbstractMachineBlock<GreenhouseBlockEntity> {
    public GreenhouseBlock() {
        super(8);
    }

    @Override
    protected GreenhouseBlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new GreenhouseBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<GreenhouseBlockEntity> machineBlockEntityType() {
        return ModBlockEntities.GREENHOUSE.get();
    }
}
