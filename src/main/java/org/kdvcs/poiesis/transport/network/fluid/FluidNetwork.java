package org.kdvcs.poiesis.transport.network.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.kdvcs.poiesis.block.FluidPipeBlock;
import org.kdvcs.poiesis.block.entity.FluidPipeBlockEntity;
import org.kdvcs.poiesis.transport.pipe.PipeEndpointMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class FluidNetwork {
    private final long id;
    private final Set<BlockPos> pipes;
    private long lastActiveTransferTick = Long.MIN_VALUE;
    private long budgetTick = Long.MIN_VALUE;
    private int transferredThisTick;
    private int sinkRotation;
    private List<FluidEndpoint> cachedSources = List.of();
    private List<FluidEndpoint> cachedSinks = List.of();
    private long endpointCacheExpires = Long.MIN_VALUE;
    private boolean endpointsDirty = true;

    public FluidNetwork(long id, Set<BlockPos> pipes) {
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
        return FluidPipeBlock.TRANSFER_RATE;
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

        List<FluidEndpoint> sources = new ArrayList<>();
        List<FluidEndpoint> sinks = new ArrayList<>();
        collectEndpoints(level, sources, sinks);

        if (sources.isEmpty() || sinks.isEmpty()) {
            return;
        }

        int sinkCount = sinks.size();
        int sinkStart = Math.floorMod(sinkRotation, sinkCount);

        for (FluidEndpoint sourceEndpoint : sources) {
            if (remainingBudget() <= 0) {
                break;
            }

            IFluidHandler source = resolveHandler(level, sourceEndpoint);

            if (source == null) {
                continue;
            }

            for (int tank = 0; tank < source.getTanks() && remainingBudget() > 0; tank++) {
                FluidStack fluidInTank = source.getFluidInTank(tank);

                if (fluidInTank.isEmpty()) {
                    continue;
                }

                FluidStack requested = fluidInTank.copy();
                requested.setAmount(Math.min(requested.getAmount(), remainingBudget()));
                FluidStack simulatedDrain = source.drain(requested, IFluidHandler.FluidAction.SIMULATE);

                if (simulatedDrain.isEmpty()) {
                    continue;
                }

                for (int offset = 0; offset < sinkCount && remainingBudget() > 0; offset++) {
                    FluidEndpoint sinkEndpoint = sinks.get((sinkStart + offset) % sinkCount);

                    if (sourceEndpoint.equals(sinkEndpoint) || sourceEndpoint.pos().equals(sinkEndpoint.pos())) {
                        continue;
                    }

                    IFluidHandler sink = resolveHandler(level, sinkEndpoint);

                    if (sink == null) {
                        continue;
                    }

                    FluidStack offered = simulatedDrain.copy();
                    offered.setAmount(Math.min(offered.getAmount(), remainingBudget()));
                    int accepted = sink.fill(offered, IFluidHandler.FluidAction.SIMULATE);

                    if (accepted <= 0) {
                        continue;
                    }

                    FluidStack exactRequest = offered.copy();
                    exactRequest.setAmount(accepted);
                    FluidStack drained = source.drain(exactRequest, IFluidHandler.FluidAction.EXECUTE);

                    if (drained.isEmpty()) {
                        break;
                    }

                    int filled = sink.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                    recordTransferred(filled);

                    if (filled < drained.getAmount()) {
                        FluidStack remainder = drained.copy();
                        remainder.setAmount(drained.getAmount() - filled);
                        source.fill(remainder, IFluidHandler.FluidAction.EXECUTE);
                    }

                    if (remainingBudget() <= 0) {
                        break;
                    }

                    fluidInTank = source.getFluidInTank(tank);

                    if (fluidInTank.isEmpty()) {
                        break;
                    }

                    requested = fluidInTank.copy();
                    requested.setAmount(Math.min(requested.getAmount(), remainingBudget()));
                    simulatedDrain = source.drain(requested, IFluidHandler.FluidAction.SIMULATE);

                    if (simulatedDrain.isEmpty()) {
                        break;
                    }
                }
            }
        }

        sinkRotation = (sinkRotation + 1) % sinkCount;
    }

    public int fillFromPipe(ServerLevel level, BlockPos inputPipePos, Direction inputSide, FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || !pipes.contains(inputPipePos)) {
            return 0;
        }

        if (!(level.getBlockEntity(inputPipePos) instanceof FluidPipeBlockEntity inputPipe)
                || inputPipe.endpointMode(inputSide) == PipeEndpointMode.DISABLED) {
            return 0;
        }

        ensureBudgetTick(level.getGameTime());

        int budget = remainingBudget();

        if (budget <= 0) {
            return 0;
        }

        List<FluidEndpoint> sinks = new ArrayList<>();
        collectSinks(level, sinks);

        if (sinks.isEmpty()) {
            return 0;
        }

        BlockPos sourceDevicePos = inputPipePos.relative(inputSide);
        int requestedAmount = Math.min(resource.getAmount(), budget);
        int remaining = requestedAmount;
        int sinkCount = sinks.size();
        int sinkStart = Math.floorMod(sinkRotation, sinkCount);

        for (int offset = 0; offset < sinkCount && remaining > 0; offset++) {
            FluidEndpoint sinkEndpoint = sinks.get((sinkStart + offset) % sinkCount);

            if (sinkEndpoint.pos().equals(sourceDevicePos)) {
                continue;
            }

            IFluidHandler sink = resolveHandler(level, sinkEndpoint);

            if (sink == null) {
                continue;
            }

            FluidStack offered = resource.copy();
            offered.setAmount(remaining);
            int accepted = sink.fill(offered, action);
            remaining -= Math.max(0, accepted);
        }

        int moved = requestedAmount - remaining;

        if (action == IFluidHandler.FluidAction.EXECUTE && moved > 0) {
            recordTransferred(moved);
            sinkRotation = (sinkRotation + 1) % sinkCount;
        }

        return moved;
    }

    public FluidStack drainToPipe(ServerLevel level, BlockPos outputPipePos, Direction outputSide, FluidStack request, IFluidHandler.FluidAction action) {
        if (request.isEmpty() || !pipes.contains(outputPipePos)) {
            return FluidStack.EMPTY;
        }

        if (!(level.getBlockEntity(outputPipePos) instanceof FluidPipeBlockEntity outputPipe)
                || outputPipe.endpointMode(outputSide) == PipeEndpointMode.DISABLED) {
            return FluidStack.EMPTY;
        }

        ensureBudgetTick(level.getGameTime());
        int allowed = Math.min(request.getAmount(), remainingBudget());

        if (allowed <= 0) {
            return FluidStack.EMPTY;
        }

        FluidStack boundedRequest = request.copy();
        boundedRequest.setAmount(allowed);
        List<FluidEndpoint> sources = new ArrayList<>();
        collectSources(level, sources);
        BlockPos destinationDevicePos = outputPipePos.relative(outputSide);

        for (FluidEndpoint sourceEndpoint : sources) {
            if (sourceEndpoint.pos().equals(destinationDevicePos)) {
                continue;
            }

            IFluidHandler source = resolveHandler(level, sourceEndpoint);

            if (source == null) {
                continue;
            }

            FluidStack drained = source.drain(boundedRequest, action);

            if (drained.isEmpty()) {
                continue;
            }

            if (action == IFluidHandler.FluidAction.EXECUTE) {
                recordTransferred(drained.getAmount());
            }

            return drained;
        }

        return FluidStack.EMPTY;
    }

    public FluidStack drainToPipe(ServerLevel level, BlockPos outputPipePos, Direction outputSide, int maxDrain, IFluidHandler.FluidAction action) {
        if (maxDrain <= 0 || !pipes.contains(outputPipePos)) {
            return FluidStack.EMPTY;
        }

        if (!(level.getBlockEntity(outputPipePos) instanceof FluidPipeBlockEntity outputPipe)
                || outputPipe.endpointMode(outputSide) == PipeEndpointMode.DISABLED) {
            return FluidStack.EMPTY;
        }

        ensureBudgetTick(level.getGameTime());
        int allowed = Math.min(maxDrain, remainingBudget());

        if (allowed <= 0) {
            return FluidStack.EMPTY;
        }

        List<FluidEndpoint> sources = new ArrayList<>();
        collectSources(level, sources);
        BlockPos destinationDevicePos = outputPipePos.relative(outputSide);

        for (FluidEndpoint sourceEndpoint : sources) {
            if (sourceEndpoint.pos().equals(destinationDevicePos)) {
                continue;
            }

            IFluidHandler source = resolveHandler(level, sourceEndpoint);

            if (source == null) {
                continue;
            }

            FluidStack drained = source.drain(allowed, action);

            if (drained.isEmpty()) {
                continue;
            }

            if (action == IFluidHandler.FluidAction.EXECUTE) {
                recordTransferred(drained.getAmount());
            }

            return drained;
        }

        return FluidStack.EMPTY;
    }

    private void collectSources(ServerLevel level, List<FluidEndpoint> sources) {
        refreshEndpointCache(level);
        sources.addAll(cachedSources);
    }

    private void collectEndpoints(ServerLevel level, List<FluidEndpoint> sources, List<FluidEndpoint> sinks) {
        refreshEndpointCache(level);
        sources.addAll(cachedSources);
        sinks.addAll(cachedSinks);
    }

    private void refreshEndpointCache(ServerLevel level) {
        long gameTime = level.getGameTime();
        if (!endpointsDirty && gameTime < endpointCacheExpires) return;
        Set<FluidEndpoint> sourceSet = new LinkedHashSet<>();
        Set<FluidEndpoint> sinkSet = new LinkedHashSet<>();

        for (BlockPos pipePos : pipes) {
            if (!(level.getBlockEntity(pipePos) instanceof FluidPipeBlockEntity pipe)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos endpointPos = pipePos.relative(direction);

                if (level.getBlockState(endpointPos).getBlock() instanceof FluidPipeBlock) {
                    continue;
                }

                PipeEndpointMode mode = pipe.endpointMode(direction);

                if (mode == PipeEndpointMode.DISABLED) {
                    continue;
                }

                FluidEndpoint endpoint = new FluidEndpoint(endpointPos, direction.getOpposite());

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

    private void collectSinks(ServerLevel level, List<FluidEndpoint> sinks) {
        refreshEndpointCache(level);
        sinks.addAll(cachedSinks);
    }

    private IFluidHandler resolveHandler(ServerLevel level, FluidEndpoint endpoint) {
        BlockEntity blockEntity = level.getBlockEntity(endpoint.pos());

        if (blockEntity == null) {
            return null;
        }

        return blockEntity
                .getCapability(ForgeCapabilities.FLUID_HANDLER, endpoint.side())
                .resolve()
                .orElse(null);
    }

    private void ensureBudgetTick(long gameTime) {
        if (budgetTick != gameTime) {
            budgetTick = gameTime;
            transferredThisTick = 0;
        }
    }

    private int remainingBudget() {
        return Math.max(0, FluidPipeBlock.TRANSFER_RATE - transferredThisTick);
    }

    private void recordTransferred(int amount) {
        if (amount > 0) {
            transferredThisTick = Math.min(FluidPipeBlock.TRANSFER_RATE, transferredThisTick + amount);
        }
    }
}
