package org.kdvcs.poiesis.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;
import org.kdvcs.poiesis.block.entity.CompounderBlockEntity;
import org.kdvcs.poiesis.registry.ModBlockEntities;

public final class CompounderBlock
        extends AbstractMachineBlock<CompounderBlockEntity> {

    public CompounderBlock() {
        super(8);
    }

    @Override
    protected CompounderBlockEntity createMachineBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new CompounderBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<CompounderBlockEntity> machineBlockEntityType() {
        return ModBlockEntities.COMPOUNDER.get();
    }
}
