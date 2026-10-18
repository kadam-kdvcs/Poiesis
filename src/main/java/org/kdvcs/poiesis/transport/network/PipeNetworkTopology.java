package org.kdvcs.poiesis.transport.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/** Resource-agnostic connected-component index shared by every pipe family. */
public final class PipeNetworkTopology<N> {
    public interface NetworkFactory<N> {
        N create(long id, Set<BlockPos> pipes);
        Set<BlockPos> pipes(N network);
    }

    private final BiPredicate<ServerLevel, BlockPos> isPipe;
    private final NetworkFactory<N> factory;
    private final Map<BlockPos, Long> pipeToNetwork = new HashMap<>();
    private final Map<Long, N> networks = new HashMap<>();
    private long nextNetworkId = 1L;

    public PipeNetworkTopology(BiPredicate<ServerLevel, BlockPos> isPipe, NetworkFactory<N> factory) {
        this.isPipe = isPipe;
        this.factory = factory;
    }

    public void addPipe(ServerLevel level, BlockPos pos) {
        if (!isPipe.test(level, pos)) return;
        Set<BlockPos> component = discoverComponent(level, pos, null);
        Set<Long> replaced = new HashSet<>();
        for (BlockPos pipePos : component) {
            Long id = pipeToNetwork.get(pipePos);
            if (id != null) replaced.add(id);
        }
        replaced.forEach(this::removeNetwork);
        createNetwork(component);
    }

    public void removePipe(ServerLevel level, BlockPos pos) {
        Long id = pipeToNetwork.remove(pos);
        if (id == null) return;
        N oldNetwork = networks.get(id);
        if (oldNetwork == null) return;
        Set<BlockPos> remaining = new LinkedHashSet<>(factory.pipes(oldNetwork));
        remaining.remove(pos);
        removeNetwork(id);
        rebuildComponents(level, remaining, pos);
    }

    public N networkFor(ServerLevel level, BlockPos pipePos) {
        N network = networkAt(pipePos);
        if (network == null) {
            addPipe(level, pipePos);
            network = networkAt(pipePos);
        }
        return network;
    }

    public N networkAt(BlockPos pipePos) {
        Long id = pipeToNetwork.get(pipePos);
        return id == null ? null : networks.get(id);
    }

    public long networkId(BlockPos pos) {
        return pipeToNetwork.getOrDefault(pos, -1L);
    }

    public void forEachNetwork(Consumer<N> action) {
        List.copyOf(networks.values()).forEach(action);
    }

    private void rebuildComponents(ServerLevel level, Set<BlockPos> candidates, BlockPos excluded) {
        Set<BlockPos> remaining = new LinkedHashSet<>();
        for (BlockPos candidate : candidates) if (isPipe.test(level, candidate)) remaining.add(candidate);
        while (!remaining.isEmpty()) {
            BlockPos seed = remaining.iterator().next();
            Set<BlockPos> component = discoverComponent(level, seed, excluded);
            component.retainAll(remaining);
            if (component.isEmpty()) remaining.remove(seed);
            else {
                createNetwork(component);
                remaining.removeAll(component);
            }
        }
    }

    private Set<BlockPos> discoverComponent(ServerLevel level, BlockPos seed, BlockPos excluded) {
        Set<BlockPos> visited = new LinkedHashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(seed);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            if (current.equals(excluded) || !isPipe.test(level, current) || !visited.add(current)) continue;
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (!neighbor.equals(excluded) && !visited.contains(neighbor) && isPipe.test(level, neighbor)) {
                    queue.addLast(neighbor);
                }
            }
        }
        return visited;
    }

    private void createNetwork(Set<BlockPos> pipes) {
        if (pipes.isEmpty()) return;
        long id = nextNetworkId++;
        N network = factory.create(id, pipes);
        networks.put(id, network);
        for (BlockPos pipe : pipes) pipeToNetwork.put(pipe.immutable(), id);
    }

    private void removeNetwork(long id) {
        N network = networks.remove(id);
        if (network == null) return;
        for (BlockPos pipe : factory.pipes(network)) {
            Long mapped = pipeToNetwork.get(pipe);
            if (mapped != null && mapped == id) pipeToNetwork.remove(pipe);
        }
    }
}
