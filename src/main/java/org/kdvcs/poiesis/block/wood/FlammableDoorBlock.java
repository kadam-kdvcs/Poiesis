package org.kdvcs.poiesis.block.wood;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/** Wooden door with the same fire behaviour as the alder planks. */
public final class FlammableDoorBlock extends DoorBlock {
    private final int fireSpreadSpeed;
    private final int flammability;

    public FlammableDoorBlock(
            Properties properties,
            BlockSetType blockSetType,
            int fireSpreadSpeed,
            int flammability
    ) {
        super(properties, blockSetType);
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
