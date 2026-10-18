package org.kdvcs.poiesis.block.wood;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Log or bark block with vanilla-style fire behaviour. */
public final class FlammablePillarBlock extends RotatedPillarBlock {
    private final int fireSpreadSpeed;
    private final int flammability;

    public FlammablePillarBlock(
            Properties properties,
            int fireSpreadSpeed,
            int flammability
    ) {
        super(properties);
        this.fireSpreadSpeed = fireSpreadSpeed;
        this.flammability = flammability;
    }

    @Override
    public int getFireSpreadSpeed(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction direction
    ) {
        return fireSpreadSpeed;
    }

    @Override
    public int getFlammability(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            Direction direction
    ) {
        return flammability;
    }
}
