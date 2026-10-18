package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.FluidPipeBlock;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.transport.network.fluid.FluidNetworkManager;
import org.kdvcs.poiesis.transport.pipe.PipeEndpointMode;

import java.util.EnumMap;
import java.util.Map;

public final class FluidPipeBlockEntity extends BlockEntity {
    private static final PipeEndpointMode DEFAULT_ENDPOINT_MODE = PipeEndpointMode.INSERT;

    private final Map<Direction, PipeEndpointMode> endpointModes = new EnumMap<>(Direction.class);
    private final Map<Direction, LazyOptional<IFluidHandler>> gatewayCapabilities = new EnumMap<>(Direction.class);

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PIPE.get(), pos, state);

        for (Direction direction : Direction.values()) {
            endpointModes.put(direction, DEFAULT_ENDPOINT_MODE);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level instanceof ServerLevel serverLevel) {
            FluidNetworkManager.get(serverLevel).addPipe(serverLevel, worldPosition);
            refreshConnections();
        }
    }

    @Override
    public void onChunkUnloaded() {
        invalidateCaps();

        if (level instanceof ServerLevel serverLevel) {
            FluidNetworkManager.get(serverLevel).removePipe(serverLevel, worldPosition);
        }

        super.onChunkUnloaded();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        gatewayCapabilities.values().forEach(LazyOptional::invalidate);
        gatewayCapabilities.clear();
    }

    public void neighborChanged() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        FluidNetworkManager.get(serverLevel).invalidateEndpoints(worldPosition);
        refreshConnections();
    }

    public void removedFromWorld() {
        invalidateCaps();

        if (level instanceof ServerLevel serverLevel) {
            FluidNetworkManager.get(serverLevel).removePipe(serverLevel, worldPosition);
        }
    }

    public PipeEndpointMode endpointMode(Direction direction) {
        return endpointModes.getOrDefault(direction, DEFAULT_ENDPOINT_MODE);
    }

    public PipeEndpointMode cycleEndpointMode(Direction direction) {
        return setEndpointMode(direction, endpointMode(direction).next());
    }

    public PipeEndpointMode cycleEndpointModeBackward(Direction direction) {
        return setEndpointMode(direction, endpointMode(direction).previous());
    }

    public PipeEndpointMode setEndpointMode(Direction direction, PipeEndpointMode mode) {
        endpointModes.put(direction, mode);
        invalidateGatewayCapability(direction);
        setChanged();
        refreshConnections();

        if (level instanceof ServerLevel serverLevel) {
            FluidNetworkManager.get(serverLevel).invalidateEndpoints(worldPosition);
        }

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }

        return mode;
    }

    public boolean hasExternalEndpoint(Direction direction) {
        if (level == null) {
            return false;
        }

        BlockPos neighborPos = worldPosition.relative(direction);

        if (level.getBlockState(neighborPos).getBlock() instanceof FluidPipeBlock) {
            return false;
        }

        BlockEntity neighborBlockEntity = level.getBlockEntity(neighborPos);

        if (neighborBlockEntity == null) {
            return false;
        }

        return neighborBlockEntity
                .getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite())
                .isPresent();
    }

    public void refreshConnections() {
        if (!(level instanceof ServerLevel)) {
            return;
        }

        BlockState currentState = getBlockState();

        if (!(currentState.getBlock() instanceof FluidPipeBlock)) {
            return;
        }

        BlockState updatedState = currentState
                .setValue(FluidPipeBlock.NORTH, canConnect(Direction.NORTH))
                .setValue(FluidPipeBlock.EAST, canConnect(Direction.EAST))
                .setValue(FluidPipeBlock.SOUTH, canConnect(Direction.SOUTH))
                .setValue(FluidPipeBlock.WEST, canConnect(Direction.WEST))
                .setValue(FluidPipeBlock.UP, canConnect(Direction.UP))
                .setValue(FluidPipeBlock.DOWN, canConnect(Direction.DOWN))
                .setValue(FluidPipeBlock.NORTH_EXTRACT, isExtractEndpoint(Direction.NORTH))
                .setValue(FluidPipeBlock.EAST_EXTRACT, isExtractEndpoint(Direction.EAST))
                .setValue(FluidPipeBlock.SOUTH_EXTRACT, isExtractEndpoint(Direction.SOUTH))
                .setValue(FluidPipeBlock.WEST_EXTRACT, isExtractEndpoint(Direction.WEST))
                .setValue(FluidPipeBlock.UP_EXTRACT, isExtractEndpoint(Direction.UP))
                .setValue(FluidPipeBlock.DOWN_EXTRACT, isExtractEndpoint(Direction.DOWN));

        if (!updatedState.equals(currentState)) {
            level.setBlock(worldPosition, updatedState, 3);
        }
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER && side != null && endpointMode(side) != PipeEndpointMode.DISABLED) {
            return gatewayCapability(side).cast();
        }

        return super.getCapability(cap, side);
    }

    private LazyOptional<IFluidHandler> gatewayCapability(Direction side) {
        return gatewayCapabilities.computeIfAbsent(
                side,
                direction -> LazyOptional.of(() -> new PipeGatewayFluidHandler(direction))
        );
    }

    private void invalidateGatewayCapability(Direction direction) {
        LazyOptional<IFluidHandler> capability = gatewayCapabilities.remove(direction);

        if (capability != null) {
            capability.invalidate();
        }
    }

    private boolean isExtractEndpoint(Direction direction) {
        if (level == null || endpointMode(direction) != PipeEndpointMode.EXTRACT) {
            return false;
        }

        BlockPos neighborPos = worldPosition.relative(direction);

        if (level.getBlockState(neighborPos).getBlock() instanceof FluidPipeBlock) {
            return false;
        }

        return hasExternalEndpoint(direction);
    }

    private boolean canConnect(Direction direction) {
        if (level == null) {
            return false;
        }

        BlockPos neighborPos = worldPosition.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);

        if (neighborState.getBlock() instanceof FluidPipeBlock) {
            return true;
        }

        if (endpointMode(direction) == PipeEndpointMode.DISABLED) {
            return false;
        }

        return hasExternalEndpoint(direction);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        CompoundTag modesTag = new CompoundTag();

        for (Direction direction : Direction.values()) {
            modesTag.putString(direction.getName(), endpointMode(direction).name());
        }

        tag.put("endpoint_modes", modesTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        for (Direction direction : Direction.values()) {
            endpointModes.put(direction, DEFAULT_ENDPOINT_MODE);
        }

        if (!tag.contains("endpoint_modes")) {
            return;
        }

        CompoundTag modesTag = tag.getCompound("endpoint_modes");

        for (Direction direction : Direction.values()) {
            String key = direction.getName();

            if (!modesTag.contains(key)) {
                continue;
            }

            endpointModes.put(direction, readEndpointMode(modesTag.getString(key)));
        }
    }

    private static PipeEndpointMode readEndpointMode(String value) {
        if ("INPUT".equals(value)) {
            return PipeEndpointMode.EXTRACT;
        }

        if ("OUTPUT".equals(value)) {
            return PipeEndpointMode.INSERT;
        }

        try {
            return PipeEndpointMode.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return DEFAULT_ENDPOINT_MODE;
        }
    }

    private final class PipeGatewayFluidHandler implements IFluidHandler {
        private final Direction side;

        private PipeGatewayFluidHandler(Direction side) {
            this.side = side;
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            if (tank != 0 || endpointMode(side) == PipeEndpointMode.DISABLED || !(level instanceof ServerLevel serverLevel)) {
                return FluidStack.EMPTY;
            }

            return FluidNetworkManager.get(serverLevel)
                    .drainFromPipe(serverLevel, worldPosition, side, Integer.MAX_VALUE, FluidAction.SIMULATE);
        }

        @Override
        public int getTankCapacity(int tank) {
            if (getBlockState().getBlock() instanceof FluidPipeBlock pipeBlock) {
                return FluidPipeBlock.TRANSFER_RATE;
            }

            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return tank == 0 && endpointMode(side) != PipeEndpointMode.DISABLED;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || endpointMode(side) == PipeEndpointMode.DISABLED) {
                return 0;
            }

            if (!(level instanceof ServerLevel serverLevel)) {
                return 0;
            }

            return FluidNetworkManager.get(serverLevel)
                    .fillFromPipe(serverLevel, worldPosition, side, resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || endpointMode(side) == PipeEndpointMode.DISABLED || !(level instanceof ServerLevel serverLevel)) {
                return FluidStack.EMPTY;
            }

            return FluidNetworkManager.get(serverLevel)
                    .drainFromPipe(serverLevel, worldPosition, side, resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0 || endpointMode(side) == PipeEndpointMode.DISABLED || !(level instanceof ServerLevel serverLevel)) {
                return FluidStack.EMPTY;
            }

            return FluidNetworkManager.get(serverLevel)
                    .drainFromPipe(serverLevel, worldPosition, side, maxDrain, action);
        }
    }
}
