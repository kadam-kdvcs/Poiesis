package org.kdvcs.poiesis.transport.network.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.kdvcs.poiesis.block.EnergyPipeBlock;
import org.kdvcs.poiesis.transport.network.PipeNetworkTopology;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class EnergyNetworkManager {
    private static final Map<ServerLevel, EnergyNetworkManager> MANAGERS = new WeakHashMap<>();
    private final PipeNetworkTopology<EnergyNetwork> topology = new PipeNetworkTopology<>(
            (level, pos) -> level.getBlockState(pos).getBlock() instanceof EnergyPipeBlock,
            new PipeNetworkTopology.NetworkFactory<>() {
                @Override public EnergyNetwork create(long id, Set<BlockPos> pipes) { return new EnergyNetwork(id, pipes); }
                @Override public Set<BlockPos> pipes(EnergyNetwork network) { return network.pipes(); }
            });

    private EnergyNetworkManager() { }

    public static EnergyNetworkManager get(ServerLevel level) {
        synchronized (MANAGERS) {
            return MANAGERS.computeIfAbsent(level, ignored -> new EnergyNetworkManager());
        }
    }

    public void addPipe(ServerLevel level, BlockPos pos) { topology.addPipe(level, pos); }
    public void removePipe(ServerLevel level, BlockPos pos) { topology.removePipe(level, pos); }
    public void tickAll(ServerLevel level) { topology.forEachNetwork(network -> network.tick(level)); }
    public void invalidateEndpoints(BlockPos pos) {
        EnergyNetwork network = topology.networkAt(pos);
        if (network != null) network.invalidateEndpoints();
    }
    public long networkId(BlockPos pos) { return topology.networkId(pos); }
}
