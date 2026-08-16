package org.kdvcs.poiesis.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;
import org.kdvcs.poiesis.block.entity.MechanicalProcessorBlockEntity;
import org.kdvcs.poiesis.registry.ModBlockEntities;

public final class MechanicalProcessorBlock
        extends AbstractMachineBlock<MechanicalProcessorBlockEntity> {

    public MechanicalProcessorBlock() {
        super(15);
    }

    @Override
    protected MechanicalProcessorBlockEntity createMachineBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new MechanicalProcessorBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<MechanicalProcessorBlockEntity> machineBlockEntityType() {
        return ModBlockEntities.MECHANICAL_PROCESSOR.get();
    }
}
