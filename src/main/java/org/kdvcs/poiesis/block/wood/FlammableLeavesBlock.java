package org.kdvcs.poiesis.block.wood;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Decaying leaves with vanilla-style leaf fire behaviour. */
public final class FlammableLeavesBlock extends LeavesBlock {
    public FlammableLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public int getFireSpreadSpeed(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction direction
    ) {
        return 30;
    }

    @Override
    public int getFlammability(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction direction
    ) {
        return 60;
    }
}
