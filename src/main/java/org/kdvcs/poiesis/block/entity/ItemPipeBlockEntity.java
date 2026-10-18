package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.ItemPipeBlock;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.transport.network.item.ItemNetworkManager;
import org.kdvcs.poiesis.transport.pipe.PipeEndpointMode;

import java.util.EnumMap;
import java.util.Map;

public final class ItemPipeBlockEntity extends BlockEntity {
    private static final PipeEndpointMode DEFAULT_ENDPOINT_MODE = PipeEndpointMode.INSERT;

    private final Map<Direction, PipeEndpointMode> endpointModes = new EnumMap<>(Direction.class);
    private final Map<Direction, LazyOptional<IItemHandler>> gatewayCapabilities = new EnumMap<>(Direction.class);

    public ItemPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_PIPE.get(), pos, state);

        for (Direction direction : Direction.values()) {
            endpointModes.put(direction, DEFAULT_ENDPOINT_MODE);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level instanceof ServerLevel serverLevel) {
            ItemNetworkManager.get(serverLevel).addPipe(serverLevel, worldPosition);
            refreshConnections();
        }
    }

    @Override
    public void onChunkUnloaded() {
        invalidateCaps();

        if (level instanceof ServerLevel serverLevel) {
            ItemNetworkManager.get(serverLevel).removePipe(serverLevel, worldPosition);
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

        ItemNetworkManager.get(serverLevel).invalidateEndpoints(worldPosition);
        refreshConnections();
    }

    public void removedFromWorld() {
        invalidateCaps();

        if (level instanceof ServerLevel serverLevel) {
            ItemNetworkManager.get(serverLevel).removePipe(serverLevel, worldPosition);
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
            ItemNetworkManager.get(serverLevel).invalidateEndpoints(worldPosition);
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

        if (level.getBlockState(neighborPos).getBlock() instanceof ItemPipeBlock) {
            return false;
        }

        BlockEntity neighborBlockEntity = level.getBlockEntity(neighborPos);

        if (neighborBlockEntity == null) {
            return false;
        }

        return neighborBlockEntity
                .getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite())
                .isPresent();
    }

    public void refreshConnections() {
        if (!(level instanceof ServerLevel)) {
            return;
        }

        BlockState currentState = getBlockState();

        if (!(currentState.getBlock() instanceof ItemPipeBlock)) {
            return;
        }

        BlockState updatedState = currentState
                .setValue(ItemPipeBlock.NORTH, canConnect(Direction.NORTH))
                .setValue(ItemPipeBlock.EAST, canConnect(Direction.EAST))
                .setValue(ItemPipeBlock.SOUTH, canConnect(Direction.SOUTH))
                .setValue(ItemPipeBlock.WEST, canConnect(Direction.WEST))
                .setValue(ItemPipeBlock.UP, canConnect(Direction.UP))
                .setValue(ItemPipeBlock.DOWN, canConnect(Direction.DOWN))
                .setValue(ItemPipeBlock.NORTH_EXTRACT, isExtractEndpoint(Direction.NORTH))
                .setValue(ItemPipeBlock.EAST_EXTRACT, isExtractEndpoint(Direction.EAST))
                .setValue(ItemPipeBlock.SOUTH_EXTRACT, isExtractEndpoint(Direction.SOUTH))
                .setValue(ItemPipeBlock.WEST_EXTRACT, isExtractEndpoint(Direction.WEST))
                .setValue(ItemPipeBlock.UP_EXTRACT, isExtractEndpoint(Direction.UP))
                .setValue(ItemPipeBlock.DOWN_EXTRACT, isExtractEndpoint(Direction.DOWN));

        if (!updatedState.equals(currentState)) {
            level.setBlock(worldPosition, updatedState, 3);
        }
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != null && endpointMode(side) != PipeEndpointMode.DISABLED) {
            return gatewayCapability(side).cast();
        }

        return super.getCapability(cap, side);
    }

    private LazyOptional<IItemHandler> gatewayCapability(Direction side) {
        return gatewayCapabilities.computeIfAbsent(
                side,
                direction -> LazyOptional.of(() -> new PipeGatewayItemHandler(direction))
        );
    }

    private void invalidateGatewayCapability(Direction direction) {
        LazyOptional<IItemHandler> capability = gatewayCapabilities.remove(direction);

        if (capability != null) {
            capability.invalidate();
        }
    }

    private boolean isExtractEndpoint(Direction direction) {
        if (level == null || endpointMode(direction) != PipeEndpointMode.EXTRACT) {
            return false;
        }

        BlockPos neighborPos = worldPosition.relative(direction);

        if (level.getBlockState(neighborPos).getBlock() instanceof ItemPipeBlock) {
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

        if (neighborState.getBlock() instanceof ItemPipeBlock) {
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

    private final class PipeGatewayItemHandler implements IItemHandler {
        private final Direction side;

        private PipeGatewayItemHandler(Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (slot != 0 || endpointMode(side) == PipeEndpointMode.DISABLED || !(level instanceof ServerLevel serverLevel)) {
                return ItemStack.EMPTY;
            }

            return ItemNetworkManager.get(serverLevel)
                    .extractToPipe(serverLevel, worldPosition, side, 64, true);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || endpointMode(side) == PipeEndpointMode.DISABLED) {
                return stack;
            }

            if (!(level instanceof ServerLevel serverLevel)) {
                return stack;
            }

            return ItemNetworkManager.get(serverLevel)
                    .insertFromPipe(serverLevel, worldPosition, side, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0 || endpointMode(side) == PipeEndpointMode.DISABLED) {
                return ItemStack.EMPTY;
            }

            if (!(level instanceof ServerLevel serverLevel)) {
                return ItemStack.EMPTY;
            }

            return ItemNetworkManager.get(serverLevel)
                    .extractToPipe(serverLevel, worldPosition, side, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == 0 && endpointMode(side) != PipeEndpointMode.DISABLED;
        }
    }
}
