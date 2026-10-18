package org.kdvcs.poiesis.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;
import org.kdvcs.poiesis.block.entity.MicrobialFuelCellBlockEntity;
import org.kdvcs.poiesis.registry.ModBlockEntities;

public final class MicrobialFuelCellBlock
        extends AbstractMachineBlock<MicrobialFuelCellBlockEntity> {
    public MicrobialFuelCellBlock() {
        super(5);
    }

    @Override
    protected MicrobialFuelCellBlockEntity createMachineBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new MicrobialFuelCellBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<MicrobialFuelCellBlockEntity> machineBlockEntityType() {
        return ModBlockEntities.MICROBIAL_FUEL_CELL.get();
    }
}
