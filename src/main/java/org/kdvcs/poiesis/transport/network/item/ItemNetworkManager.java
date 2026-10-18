package org.kdvcs.poiesis.transport.network.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.block.ItemPipeBlock;
import org.kdvcs.poiesis.transport.network.PipeNetworkTopology;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class ItemNetworkManager {
    private static final Map<ServerLevel, ItemNetworkManager> MANAGERS = new WeakHashMap<>();
    private final PipeNetworkTopology<ItemNetwork> topology = new PipeNetworkTopology<>(
            (level, pos) -> level.getBlockState(pos).getBlock() instanceof ItemPipeBlock,
            new PipeNetworkTopology.NetworkFactory<>() {
                @Override public ItemNetwork create(long id, Set<BlockPos> pipes) { return new ItemNetwork(id, pipes); }
                @Override public Set<BlockPos> pipes(ItemNetwork network) { return network.pipes(); }
            });

    private ItemNetworkManager() { }

    public static ItemNetworkManager get(ServerLevel level) {
        synchronized (MANAGERS) {
            return MANAGERS.computeIfAbsent(level, ignored -> new ItemNetworkManager());
        }
    }

    public void addPipe(ServerLevel level, BlockPos pos) { topology.addPipe(level, pos); }
    public void removePipe(ServerLevel level, BlockPos pos) { topology.removePipe(level, pos); }
    public void tickAll(ServerLevel level) { topology.forEachNetwork(network -> network.tick(level)); }
    public void invalidateEndpoints(BlockPos pos) {
        ItemNetwork network = topology.networkAt(pos);
        if (network != null) network.invalidateEndpoints();
    }
    public ItemStack insertFromPipe(ServerLevel level, BlockPos pos, Direction side, ItemStack stack, boolean simulate) {
        ItemNetwork network = topology.networkFor(level, pos);
        return network == null ? stack : network.insertFromPipe(level, pos, side, stack, simulate);
    }
    public ItemStack extractToPipe(ServerLevel level, BlockPos pos, Direction side, int amount, boolean simulate) {
        ItemNetwork network = topology.networkFor(level, pos);
        return network == null ? ItemStack.EMPTY : network.extractToPipe(level, pos, side, amount, simulate);
    }
    public long networkId(BlockPos pos) { return topology.networkId(pos); }
}
