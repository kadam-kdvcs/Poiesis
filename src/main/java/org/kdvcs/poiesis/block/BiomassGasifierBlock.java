package org.kdvcs.poiesis.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;
import org.kdvcs.poiesis.block.entity.BiomassGasifierBlockEntity;
import org.kdvcs.poiesis.registry.ModBlockEntities;

public final class BiomassGasifierBlock
        extends AbstractMachineBlock<BiomassGasifierBlockEntity> {
    public BiomassGasifierBlock() {
        super(8);
    }

    @Override
    protected BiomassGasifierBlockEntity createMachineBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new BiomassGasifierBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<BiomassGasifierBlockEntity> machineBlockEntityType() {
        return ModBlockEntities.BIOMASS_GASIFIER.get();
    }
}
