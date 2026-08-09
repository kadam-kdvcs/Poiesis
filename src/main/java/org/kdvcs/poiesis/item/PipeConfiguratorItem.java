package org.kdvcs.poiesis.item;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.kdvcs.poiesis.block.entity.FluidPipeBlockEntity;
import org.kdvcs.poiesis.block.entity.ItemPipeBlockEntity;
import org.kdvcs.poiesis.transport.pipe.PipeEndpointMode;

public final class PipeConfiguratorItem extends Item {
    public PipeConfiguratorItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockEntity blockEntity = context.getLevel().getBlockEntity(context.getClickedPos());
        Direction side = resolveTargetSide(context);

        if (!(blockEntity instanceof ItemPipeBlockEntity) && !(blockEntity instanceof FluidPipeBlockEntity)) {
            return InteractionResult.PASS;
        }

        if (context.getLevel().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        boolean reverse = context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
        PipeEndpointMode mode = null;

        if (blockEntity instanceof ItemPipeBlockEntity itemPipe && itemPipe.hasExternalEndpoint(side)) {
            mode = reverse ? itemPipe.cycleEndpointModeBackward(side) : itemPipe.cycleEndpointMode(side);
        } else if (blockEntity instanceof FluidPipeBlockEntity fluidPipe && fluidPipe.hasExternalEndpoint(side)) {
            mode = reverse ? fluidPipe.cycleEndpointModeBackward(side) : fluidPipe.cycleEndpointMode(side);
        }

        if (mode == null) {
            return InteractionResult.PASS;
        }

        if (context.getPlayer() != null) {
            context.getPlayer().displayClientMessage(
                    Component.translatable("message.poiesis.pipe_endpoint_mode", Component.translatable(modeTranslationKey(mode))),
                    true
            );
        }

        return InteractionResult.CONSUME;
    }

    private static Direction resolveTargetSide(UseOnContext context) {
        double x = context.getClickLocation().x - context.getClickedPos().getX() - 0.5D;
        double y = context.getClickLocation().y - context.getClickedPos().getY() - 0.5D;
        double z = context.getClickLocation().z - context.getClickedPos().getZ() - 0.5D;

        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);

        if (absX >= absY && absX >= absZ) {
            return x >= 0.0D ? Direction.EAST : Direction.WEST;
        }

        if (absY >= absX && absY >= absZ) {
            return y >= 0.0D ? Direction.UP : Direction.DOWN;
        }

        return z >= 0.0D ? Direction.SOUTH : Direction.NORTH;
    }

    private static String modeTranslationKey(PipeEndpointMode mode) {
        return switch (mode) {
            case INSERT -> "message.poiesis.pipe_mode.insert";
            case EXTRACT -> "message.poiesis.pipe_mode.extract";
            case DISABLED -> "message.poiesis.pipe_mode.disabled";
        };
    }
}
