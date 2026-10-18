package org.kdvcs.poiesis.block.wood;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Wooden stairs with the same fire behaviour as the alder planks. */
public final class FlammableStairBlock extends StairBlock {
    private final int fireSpreadSpeed;
    private final int flammability;

    public FlammableStairBlock(
            BlockState baseState,
            Properties properties,
            int fireSpreadSpeed,
            int flammability
    ) {
        super(baseState, properties);
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
