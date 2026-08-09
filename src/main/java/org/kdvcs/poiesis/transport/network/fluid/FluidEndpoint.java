package org.kdvcs.poiesis.transport.network.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record FluidEndpoint(BlockPos pos, Direction side) {
}
