package org.kdvcs.poiesis.block.wood;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

/** Wooden button with the same fire behaviour as the alder planks. */
public final class FlammableButtonBlock extends ButtonBlock {
    private final int fireSpreadSpeed;
    private final int flammability;

    public FlammableButtonBlock(
            Properties properties,
            BlockSetType blockSetType,
            int ticksToStayPressed,
            boolean arrowsCanPress,
            int fireSpreadSpeed,
            int flammability
    ) {
        super(properties, blockSetType, ticksToStayPressed, arrowsCanPress);
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
