package org.kdvcs.poiesis.transport.network.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.kdvcs.poiesis.block.FluidPipeBlock;
import org.kdvcs.poiesis.transport.network.PipeNetworkTopology;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class FluidNetworkManager {
    private static final Map<ServerLevel, FluidNetworkManager> MANAGERS = new WeakHashMap<>();
    private final PipeNetworkTopology<FluidNetwork> topology = new PipeNetworkTopology<>(
            (level, pos) -> level.getBlockState(pos).getBlock() instanceof FluidPipeBlock,
            new PipeNetworkTopology.NetworkFactory<>() {
                @Override public FluidNetwork create(long id, Set<BlockPos> pipes) { return new FluidNetwork(id, pipes); }
                @Override public Set<BlockPos> pipes(FluidNetwork network) { return network.pipes(); }
            });

    private FluidNetworkManager() { }

    public static FluidNetworkManager get(ServerLevel level) {
        synchronized (MANAGERS) {
            return MANAGERS.computeIfAbsent(level, ignored -> new FluidNetworkManager());
        }
    }

    public void addPipe(ServerLevel level, BlockPos pos) { topology.addPipe(level, pos); }
    public void removePipe(ServerLevel level, BlockPos pos) { topology.removePipe(level, pos); }
    public void tickAll(ServerLevel level) { topology.forEachNetwork(network -> network.tick(level)); }
    public void invalidateEndpoints(BlockPos pos) {
        FluidNetwork network = topology.networkAt(pos);
        if (network != null) network.invalidateEndpoints();
    }
    public int fillFromPipe(ServerLevel level, BlockPos pos, Direction side, FluidStack resource, IFluidHandler.FluidAction action) {
        FluidNetwork network = topology.networkFor(level, pos);
        return network == null ? 0 : network.fillFromPipe(level, pos, side, resource, action);
    }
    public FluidStack drainFromPipe(ServerLevel level, BlockPos pos, Direction side, FluidStack request, IFluidHandler.FluidAction action) {
        FluidNetwork network = topology.networkFor(level, pos);
        return network == null ? FluidStack.EMPTY : network.drainToPipe(level, pos, side, request, action);
    }
    public FluidStack drainFromPipe(ServerLevel level, BlockPos pos, Direction side, int amount, IFluidHandler.FluidAction action) {
        FluidNetwork network = topology.networkFor(level, pos);
        return network == null ? FluidStack.EMPTY : network.drainToPipe(level, pos, side, amount, action);
    }
    public long networkId(BlockPos pos) { return topology.networkId(pos); }
}
