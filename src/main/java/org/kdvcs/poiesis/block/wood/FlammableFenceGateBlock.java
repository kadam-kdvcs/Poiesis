package org.kdvcs.poiesis.block.wood;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

/** Wooden fence gate with the same fire behaviour as the alder planks. */
public final class FlammableFenceGateBlock extends FenceGateBlock {
    private final int fireSpreadSpeed;
    private final int flammability;

    public FlammableFenceGateBlock(
            Properties properties,
            WoodType woodType,
            int fireSpreadSpeed,
            int flammability
    ) {
        super(properties, woodType);
        this.fireSpreadSpeed = fireSpreadSpeed;
        this.flammability = flammability;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return fireSpreadSpeed;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return flammability;
    }
}
