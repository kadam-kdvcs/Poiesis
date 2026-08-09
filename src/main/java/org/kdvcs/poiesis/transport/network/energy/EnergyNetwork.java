package org.kdvcs.poiesis.transport.network.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.kdvcs.poiesis.block.EnergyPipeBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EnergyNetwork {
    private final long id;
    private final Set<BlockPos> pipes;
    private long lastTransferTick = Long.MIN_VALUE;
    private int sinkRotation;

    public EnergyNetwork(long id, Set<BlockPos> pipes) {
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
        return EnergyPipeBlock.TRANSFER_RATE;
    }

    public void tick(ServerLevel level) {
        long gameTime = level.getGameTime();

        if (lastTransferTick == gameTime) {
            return;
        }

        lastTransferTick = gameTime;

        if (EnergyPipeBlock.TRANSFER_RATE <= 0 || pipes.isEmpty()) {
            return;
        }

        List<EnergyEndpoint> sources = new ArrayList<>();
        List<EnergyEndpoint> sinks = new ArrayList<>();
        collectEndpoints(level, sources, sinks);

        if (sources.isEmpty() || sinks.isEmpty()) {
            return;
        }

        int transferred = 0;
        int sinkCount = sinks.size();
        int sinkStart = Math.floorMod(sinkRotation, sinkCount);

        for (EnergyEndpoint sourceEndpoint : sources) {
            if (transferred >= EnergyPipeBlock.TRANSFER_RATE) {
                break;
            }

            IEnergyStorage source = resolveStorage(level, sourceEndpoint);

            if (source == null || !source.canExtract()) {
                continue;
            }

            for (int offset = 0; offset < sinkCount && transferred < EnergyPipeBlock.TRANSFER_RATE; offset++) {
                EnergyEndpoint sinkEndpoint = sinks.get((sinkStart + offset) % sinkCount);

                if (sourceEndpoint.pos().equals(sinkEndpoint.pos())) {
                    continue;
                }

                IEnergyStorage sink = resolveStorage(level, sinkEndpoint);

                if (sink == null || !sink.canReceive()) {
                    continue;
                }

                int remainingNetworkCapacity = EnergyPipeBlock.TRANSFER_RATE - transferred;
                int available = source.extractEnergy(remainingNetworkCapacity, true);

                if (available <= 0) {
                    break;
                }

                int accepted = sink.receiveEnergy(available, true);

                if (accepted <= 0) {
                    continue;
                }

                int extracted = source.extractEnergy(accepted, false);

                if (extracted <= 0) {
                    break;
                }

                int received = sink.receiveEnergy(extracted, false);
                transferred += received;

                if (received < extracted) {
                    break;
                }
            }
        }

        sinkRotation = (sinkRotation + 1) % sinkCount;
    }

    private void collectEndpoints(
            ServerLevel level,
            List<EnergyEndpoint> sources,
            List<EnergyEndpoint> sinks
    ) {
        Set<EnergyEndpoint> visited = new LinkedHashSet<>();

        for (BlockPos pipePos : pipes) {
            for (Direction direction : Direction.values()) {
                BlockPos endpointPos = pipePos.relative(direction);

                if (level.getBlockState(endpointPos).getBlock() instanceof EnergyPipeBlock) {
                    continue;
                }

                EnergyEndpoint endpoint = new EnergyEndpoint(endpointPos, direction.getOpposite());

                if (!visited.add(endpoint)) {
                    continue;
                }

                IEnergyStorage storage = resolveStorage(level, endpoint);

                if (storage == null) {
                    continue;
                }

                if (storage.canExtract()) {
                    sources.add(endpoint);
                }

                if (storage.canReceive()) {
                    sinks.add(endpoint);
                }
            }
        }
    }

    private IEnergyStorage resolveStorage(ServerLevel level, EnergyEndpoint endpoint) {
        BlockEntity blockEntity = level.getBlockEntity(endpoint.pos());

        if (blockEntity == null) {
            return null;
        }

        return blockEntity
                .getCapability(ForgeCapabilities.ENERGY, endpoint.side())
                .resolve()
                .orElse(null);
    }
}
