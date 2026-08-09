package org.kdvcs.poiesis.transport.network.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.kdvcs.poiesis.block.EnergyPipeBlock;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class EnergyNetworkManager {
    private static final Map<ServerLevel, EnergyNetworkManager> MANAGERS = new WeakHashMap<>();

    private final Map<BlockPos, Long> pipeToNetwork = new HashMap<>();
    private final Map<Long, EnergyNetwork> networks = new HashMap<>();
    private long nextNetworkId = 1L;

    private EnergyNetworkManager() {
    }

    public static EnergyNetworkManager get(ServerLevel level) {
        synchronized (MANAGERS) {
            return MANAGERS.computeIfAbsent(level, ignored -> new EnergyNetworkManager());
        }
    }

    public void addPipe(ServerLevel level, BlockPos pos) {
        if (!isEnergyPipe(level, pos)) {
            return;
        }

        Set<BlockPos> component = discoverComponent(level, pos, null);
        Set<Long> replacedNetworks = new HashSet<>();

        for (BlockPos pipePos : component) {
            Long networkId = pipeToNetwork.get(pipePos);

            if (networkId != null) {
                replacedNetworks.add(networkId);
            }
        }

        for (Long networkId : replacedNetworks) {
            removeNetwork(networkId);
        }

        createNetwork(level, component);
    }

    public void removePipe(ServerLevel level, BlockPos pos) {
        Long networkId = pipeToNetwork.get(pos);

        if (networkId == null) {
            pipeToNetwork.remove(pos);
            return;
        }

        EnergyNetwork oldNetwork = networks.get(networkId);

        if (oldNetwork == null) {
            pipeToNetwork.remove(pos);
            return;
        }

        Set<BlockPos> remaining = new LinkedHashSet<>(oldNetwork.pipes());
        remaining.remove(pos);
        removeNetwork(networkId);

        rebuildComponents(level, remaining, pos);
    }

    public void tickPipe(ServerLevel level, BlockPos pos) {
        Long networkId = pipeToNetwork.get(pos);

        if (networkId == null) {
            addPipe(level, pos);
            networkId = pipeToNetwork.get(pos);
        }

        if (networkId == null) {
            return;
        }

        EnergyNetwork network = networks.get(networkId);

        if (network != null) {
            network.tick(level);
        }
    }

    public long networkId(BlockPos pos) {
        return pipeToNetwork.getOrDefault(pos, -1L);
    }

    private void rebuildComponents(ServerLevel level, Set<BlockPos> candidates, BlockPos excludedPos) {
        Set<BlockPos> remaining = new LinkedHashSet<>();

        for (BlockPos candidate : candidates) {
            if (isEnergyPipe(level, candidate)) {
                remaining.add(candidate);
            }
        }

        while (!remaining.isEmpty()) {
            BlockPos seed = remaining.iterator().next();
            Set<BlockPos> component = discoverComponent(level, seed, excludedPos);
            component.retainAll(remaining);

            if (component.isEmpty()) {
                remaining.remove(seed);
                continue;
            }

            createNetwork(level, component);
            remaining.removeAll(component);
        }
    }

    private Set<BlockPos> discoverComponent(ServerLevel level, BlockPos seed, BlockPos excludedPos) {
        Set<BlockPos> visited = new LinkedHashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(seed);

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();

            if (!visited.add(current) || current.equals(excludedPos) || !isEnergyPipe(level, current)) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);

                if (!neighbor.equals(excludedPos) && !visited.contains(neighbor) && isEnergyPipe(level, neighbor)) {
                    queue.addLast(neighbor);
                }
            }
        }

        visited.removeIf(pos -> pos.equals(excludedPos) || !isEnergyPipe(level, pos));
        return visited;
    }

    private void createNetwork(ServerLevel level, Set<BlockPos> pipes) {
        if (pipes.isEmpty()) {
            return;
        }

        long id = nextNetworkId++;
        EnergyNetwork network = new EnergyNetwork(id, pipes);
        networks.put(id, network);

        for (BlockPos pipePos : pipes) {
            pipeToNetwork.put(pipePos.immutable(), id);
        }
    }

    private void removeNetwork(long networkId) {
        EnergyNetwork network = networks.remove(networkId);

        if (network == null) {
            return;
        }

        for (BlockPos pipePos : network.pipes()) {
            Long mappedNetwork = pipeToNetwork.get(pipePos);

            if (mappedNetwork != null && mappedNetwork == networkId) {
                pipeToNetwork.remove(pipePos);
            }
        }
    }

    private boolean isEnergyPipe(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof EnergyPipeBlock;
    }
}
