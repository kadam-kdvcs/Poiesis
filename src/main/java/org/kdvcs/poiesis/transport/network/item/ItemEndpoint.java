package org.kdvcs.poiesis.transport.network.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record ItemEndpoint(BlockPos pos, Direction side) {
}
