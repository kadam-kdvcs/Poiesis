package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.kdvcs.poiesis.block.EnergyPipeBlock;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.transport.network.energy.EnergyNetworkManager;

public final class EnergyPipeBlockEntity extends BlockEntity {
    private long lastConnectionRefresh = Long.MIN_VALUE;

    public EnergyPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_PIPE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EnergyPipeBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        EnergyNetworkManager.get(serverLevel).tickPipe(serverLevel, pos);

        if (level.getGameTime() - blockEntity.lastConnectionRefresh >= 20L) {
            blockEntity.refreshConnections();
            blockEntity.lastConnectionRefresh = level.getGameTime();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level instanceof ServerLevel serverLevel) {
            EnergyNetworkManager.get(serverLevel).addPipe(serverLevel, worldPosition);
            refreshConnections();
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (level instanceof ServerLevel serverLevel) {
            EnergyNetworkManager.get(serverLevel).removePipe(serverLevel, worldPosition);
        }

        super.onChunkUnloaded();
    }

    public void neighborChanged() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        refreshConnections();
    }

    public void removedFromWorld() {
        if (level instanceof ServerLevel serverLevel) {
            EnergyNetworkManager.get(serverLevel).removePipe(serverLevel, worldPosition);
        }
    }

    public void refreshConnections() {
        if (!(level instanceof ServerLevel)) {
            return;
        }

        BlockState currentState = getBlockState();

        if (!(currentState.getBlock() instanceof EnergyPipeBlock)) {
            return;
        }

        BlockState updatedState = currentState
                .setValue(EnergyPipeBlock.NORTH, canConnect(Direction.NORTH))
                .setValue(EnergyPipeBlock.EAST, canConnect(Direction.EAST))
                .setValue(EnergyPipeBlock.SOUTH, canConnect(Direction.SOUTH))
                .setValue(EnergyPipeBlock.WEST, canConnect(Direction.WEST))
                .setValue(EnergyPipeBlock.UP, canConnect(Direction.UP))
                .setValue(EnergyPipeBlock.DOWN, canConnect(Direction.DOWN));

        if (!updatedState.equals(currentState)) {
            level.setBlock(worldPosition, updatedState, 3);
        }
    }

    private boolean canConnect(Direction direction) {
        if (level == null) {
            return false;
        }

        BlockPos neighborPos = worldPosition.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);

        if (neighborState.getBlock() instanceof EnergyPipeBlock) {
            return true;
        }

        BlockEntity neighborBlockEntity = level.getBlockEntity(neighborPos);

        if (neighborBlockEntity == null) {
            return false;
        }

        return neighborBlockEntity
                .getCapability(ForgeCapabilities.ENERGY, direction.getOpposite())
                .isPresent();
    }
}
