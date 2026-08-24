package org.kdvcs.poiesis.machine.io.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideConfigurationComponent;

/** Active fluid transfer for the column's remote input and separate output tanks. */
public final class AdsorptionFluidAutoTransfer {
    private static final int MAX_TRANSFER_PER_CYCLE = 1_000;

    private AdsorptionFluidAutoTransfer() {
    }

    public static void pushFromControllerSides(
            MachineContext owner,
            IFluidHandler source,
            SideConfigurationComponent configuration
    ) {
        Level level = owner.level();
        if (level == null || level.isClientSide) {
            return;
        }

        for (RelativeSide side : RelativeSide.values()) {
            if (side == RelativeSide.TOP
                    || side == RelativeSide.BOTTOM
                    || !configuration.allowsOutput(IoChannel.FLUID, side)) {
                continue;
            }

            Direction worldSide = side.toWorldSide(owner.facing());
            IFluidHandler destination = capabilityAt(
                    level,
                    owner.pos().relative(worldSide),
                    worldSide.getOpposite()
            );
            if (destination != null) {
                FluidUtil.tryFluidTransfer(
                        destination,
                        source,
                        MAX_TRANSFER_PER_CYCLE,
                        true
                );
            }
        }
    }

    private static IFluidHandler capabilityAt(
            Level level,
            BlockPos pos,
            Direction queriedSide
    ) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return null;
        }
        return blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, queriedSide)
                .resolve()
                .orElse(null);
    }
}
