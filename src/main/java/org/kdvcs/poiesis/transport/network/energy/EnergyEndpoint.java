package org.kdvcs.poiesis.transport.network.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record EnergyEndpoint(BlockPos pos, Direction side) {
}
