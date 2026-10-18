package org.kdvcs.poiesis.transport.network.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.kdvcs.poiesis.block.ItemPipeBlock;
import org.kdvcs.poiesis.block.entity.ItemPipeBlockEntity;
import org.kdvcs.poiesis.transport.pipe.PipeEndpointMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ItemNetwork {
    private final long id;
    private final Set<BlockPos> pipes;
    private long lastActiveTransferTick = Long.MIN_VALUE;
    private long budgetTick = Long.MIN_VALUE;
    private int transferredThisTick;
    private int sinkRotation;
    private List<ItemEndpoint> cachedSources = List.of();
    private List<ItemEndpoint> cachedSinks = List.of();
    private long endpointCacheExpires = Long.MIN_VALUE;
    private boolean endpointsDirty = true;

    public ItemNetwork(long id, Set<BlockPos> pipes) {
        this.id = id;
        this.pipes = Collections.unmodifiableSet(new LinkedHashSet<>(pipes));
    }

    public long id() {
        return id;
    }

    public Set<BlockPos> pipes() {
        return pipes;
    }

    public int transferLimit() {
        return ItemPipeBlock.TRANSFER_RATE;
    }

    public void invalidateEndpoints() {
        endpointsDirty = true;
    }

    public void tick(ServerLevel level) {
        long gameTime = level.getGameTime();

        if (lastActiveTransferTick == gameTime) {
            return;
        }

        lastActiveTransferTick = gameTime;
        ensureBudgetTick(gameTime);

        if (remainingBudget() <= 0 || pipes.isEmpty()) {
            return;
        }

        List<ItemEndpoint> sources = new ArrayList<>();
        List<ItemEndpoint> sinks = new ArrayList<>();
        collectEndpoints(level, sources, sinks);

        if (sources.isEmpty() || sinks.isEmpty()) {
            return;
        }

        int sinkCount = sinks.size();
        int sinkStart = Math.floorMod(sinkRotation, sinkCount);

        for (ItemEndpoint sourceEndpoint : sources) {
            if (remainingBudget() <= 0) {
                break;
            }

            IItemHandler source = resolveHandler(level, sourceEndpoint);

            if (source == null) {
                continue;
            }

            for (int sourceSlot = 0; sourceSlot < source.getSlots() && remainingBudget() > 0; sourceSlot++) {
                ItemStack simulatedExtract = source.extractItem(sourceSlot, remainingBudget(), true);

                if (simulatedExtract.isEmpty()) {
                    continue;
                }

                for (int offset = 0; offset < sinkCount && remainingBudget() > 0; offset++) {
                    ItemEndpoint sinkEndpoint = sinks.get((sinkStart + offset) % sinkCount);

                    if (sourceEndpoint.equals(sinkEndpoint) || sourceEndpoint.pos().equals(sinkEndpoint.pos())) {
                        continue;
                    }

                    IItemHandler sink = resolveHandler(level, sinkEndpoint);

                    if (sink == null) {
                        continue;
                    }

                    int allowed = Math.min(simulatedExtract.getCount(), remainingBudget());
                    ItemStack offered = simulatedExtract.copy();
                    offered.setCount(allowed);
                    ItemStack simulatedRemainder = insertInto(sink, offered, true);
                    int accepted = offered.getCount() - simulatedRemainder.getCount();

                    if (accepted <= 0) {
                        continue;
                    }

                    ItemStack extracted = source.extractItem(sourceSlot, accepted, false);

                    if (extracted.isEmpty()) {
                        break;
                    }

                    ItemStack remainder = insertInto(sink, extracted, false);
                    int inserted = extracted.getCount() - remainder.getCount();
                    recordTransferred(inserted);

                    if (!remainder.isEmpty()) {
                        source.insertItem(sourceSlot, remainder, false);
                    }

                    if (remainingBudget() <= 0) {
                        break;
                    }

                    simulatedExtract = source.extractItem(sourceSlot, remainingBudget(), true);

                    if (simulatedExtract.isEmpty()) {
                        break;
                    }
                }
            }
        }

        sinkRotation = (sinkRotation + 1) % sinkCount;
    }

    public ItemStack insertFromPipe(ServerLevel level, BlockPos inputPipePos, Direction inputSide, ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !pipes.contains(inputPipePos)) {
            return stack;
        }

        if (!(level.getBlockEntity(inputPipePos) instanceof ItemPipeBlockEntity inputPipe)
                || inputPipe.endpointMode(inputSide) == PipeEndpointMode.DISABLED) {
            return stack;
        }

        ensureBudgetTick(level.getGameTime());

        int budget = remainingBudget();

        if (budget <= 0) {
            return stack;
        }

        List<ItemEndpoint> sinks = new ArrayList<>();
        collectSinks(level, sinks);

        if (sinks.isEmpty()) {
            return stack;
        }

        BlockPos sourceDevicePos = inputPipePos.relative(inputSide);
        ItemStack remaining = stack.copy();
        int allowed = Math.min(remaining.getCount(), budget);
        remaining.setCount(allowed);
        int originalAllowed = allowed;

        int sinkCount = sinks.size();
        int sinkStart = Math.floorMod(sinkRotation, sinkCount);

        for (int offset = 0; offset < sinkCount && !remaining.isEmpty(); offset++) {
            ItemEndpoint sinkEndpoint = sinks.get((sinkStart + offset) % sinkCount);

            if (sinkEndpoint.pos().equals(sourceDevicePos)) {
                continue;
            }

            IItemHandler sink = resolveHandler(level, sinkEndpoint);

            if (sink == null) {
                continue;
            }

            remaining = insertInto(sink, remaining, simulate);
        }

        int moved = originalAllowed - remaining.getCount();

        if (!simulate && moved > 0) {
            recordTransferred(moved);
            sinkRotation = (sinkRotation + 1) % sinkCount;
        }

        ItemStack fullRemainder = stack.copy();
        fullRemainder.shrink(moved);
        return fullRemainder;
    }

    public ItemStack extractToPipe(ServerLevel level, BlockPos outputPipePos, Direction outputSide, int amount, boolean simulate) {
        if (amount <= 0 || !pipes.contains(outputPipePos)) {
            return ItemStack.EMPTY;
        }

        if (!(level.getBlockEntity(outputPipePos) instanceof ItemPipeBlockEntity outputPipe)
                || outputPipe.endpointMode(outputSide) == PipeEndpointMode.DISABLED) {
            return ItemStack.EMPTY;
        }

        ensureBudgetTick(level.getGameTime());
        int allowed = Math.min(amount, remainingBudget());

        if (allowed <= 0) {
            return ItemStack.EMPTY;
        }

        List<ItemEndpoint> sources = new ArrayList<>();
        collectSources(level, sources);
        BlockPos destinationDevicePos = outputPipePos.relative(outputSide);

        for (ItemEndpoint sourceEndpoint : sources) {
            if (sourceEndpoint.pos().equals(destinationDevicePos)) {
                continue;
            }

            IItemHandler source = resolveHandler(level, sourceEndpoint);

            if (source == null) {
                continue;
            }

            for (int sourceSlot = 0; sourceSlot < source.getSlots(); sourceSlot++) {
                ItemStack extracted = source.extractItem(sourceSlot, allowed, simulate);

                if (extracted.isEmpty()) {
                    continue;
                }

                if (!simulate) {
                    recordTransferred(extracted.getCount());
                }

                return extracted;
            }
        }

        return ItemStack.EMPTY;
    }

    private void collectSources(ServerLevel level, List<ItemEndpoint> sources) {
        refreshEndpointCache(level);
        sources.addAll(cachedSources);
    }

    private void collectEndpoints(ServerLevel level, List<ItemEndpoint> sources, List<ItemEndpoint> sinks) {
        refreshEndpointCache(level);
        sources.addAll(cachedSources);
        sinks.addAll(cachedSinks);
    }

    private void refreshEndpointCache(ServerLevel level) {
        long gameTime = level.getGameTime();
        if (!endpointsDirty && gameTime < endpointCacheExpires) return;
        Set<ItemEndpoint> sourceSet = new LinkedHashSet<>();
        Set<ItemEndpoint> sinkSet = new LinkedHashSet<>();

        for (BlockPos pipePos : pipes) {
            if (!(level.getBlockEntity(pipePos) instanceof ItemPipeBlockEntity pipe)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos endpointPos = pipePos.relative(direction);

                if (level.getBlockState(endpointPos).getBlock() instanceof ItemPipeBlock) {
                    continue;
                }

                PipeEndpointMode mode = pipe.endpointMode(direction);

                if (mode == PipeEndpointMode.DISABLED) {
                    continue;
                }

                ItemEndpoint endpoint = new ItemEndpoint(endpointPos, direction.getOpposite());

                if (resolveHandler(level, endpoint) == null) {
                    continue;
                }

                if (mode == PipeEndpointMode.EXTRACT) {
                    sourceSet.add(endpoint);
                } else if (mode == PipeEndpointMode.INSERT) {
                    sinkSet.add(endpoint);
                }
            }
        }

        cachedSources = List.copyOf(sourceSet);
        cachedSinks = List.copyOf(sinkSet);
        endpointCacheExpires = gameTime + 100L;
        endpointsDirty = false;
    }

    private void collectSinks(ServerLevel level, List<ItemEndpoint> sinks) {
        refreshEndpointCache(level);
        sinks.addAll(cachedSinks);
    }

    private IItemHandler resolveHandler(ServerLevel level, ItemEndpoint endpoint) {
        BlockEntity blockEntity = level.getBlockEntity(endpoint.pos());

        if (blockEntity == null) {
            return null;
        }

        return blockEntity
                .getCapability(ForgeCapabilities.ITEM_HANDLER, endpoint.side())
                .resolve()
                .orElse(null);
    }

    private ItemStack insertInto(IItemHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack.copy();

        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = handler.insertItem(slot, remaining, simulate);
        }

        return remaining;
    }

    private void ensureBudgetTick(long gameTime) {
        if (budgetTick != gameTime) {
            budgetTick = gameTime;
            transferredThisTick = 0;
        }
    }

    private int remainingBudget() {
        return Math.max(0, ItemPipeBlock.TRANSFER_RATE - transferredThisTick);
    }

    private void recordTransferred(int amount) {
        if (amount > 0) {
            transferredThisTick = Math.min(ItemPipeBlock.TRANSFER_RATE, transferredThisTick + amount);
        }
    }
}
