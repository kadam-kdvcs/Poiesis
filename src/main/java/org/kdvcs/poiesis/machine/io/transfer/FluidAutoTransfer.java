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

/** Active output for ordinary single-tank machines. */
public final class FluidAutoTransfer {
    private FluidAutoTransfer() {
    }

    public static void pullInputs(
            MachineContext owner,
            IFluidHandler destination,
            SideConfigurationComponent configuration,
            int maximumPerSide
    ) {
        Level level = owner.level();
        if (level == null || level.isClientSide || maximumPerSide <= 0) return;

        for (RelativeSide side : RelativeSide.values()) {
            if (!configuration.allowsInput(IoChannel.FLUID, side)) continue;
            Direction worldSide = side.toWorldSide(owner.facing());
            BlockEntity blockEntity = level.getBlockEntity(owner.pos().relative(worldSide));
            if (blockEntity == null) continue;
            blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, worldSide.getOpposite())
                    .ifPresent(source -> FluidUtil.tryFluidTransfer(
                            destination, source, maximumPerSide, true
                    ));
        }
    }

    public static void pushOutputs(
            MachineContext owner,
            IFluidHandler source,
            SideConfigurationComponent configuration,
            int maximumPerSide
    ) {
        Level level = owner.level();
        if (level == null || level.isClientSide || maximumPerSide <= 0) {
            return;
        }

        for (RelativeSide side : RelativeSide.values()) {
            if (!configuration.allowsOutput(IoChannel.FLUID, side)) {
                continue;
            }

            Direction worldSide = side.toWorldSide(owner.facing());
            BlockPos destinationPos = owner.pos().relative(worldSide);
            BlockEntity blockEntity = level.getBlockEntity(destinationPos);
            if (blockEntity == null) {
                continue;
            }

            blockEntity.getCapability(
                    ForgeCapabilities.FLUID_HANDLER,
                    worldSide.getOpposite()
            ).ifPresent(destination -> FluidUtil.tryFluidTransfer(
                    destination,
                    source,
                    maximumPerSide,
                    true
            ));
        }
    }
}
