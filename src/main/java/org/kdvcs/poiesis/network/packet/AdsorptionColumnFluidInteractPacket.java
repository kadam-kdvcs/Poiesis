package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.block.entity.AdsorptionColumnControllerBlockEntity;
import org.kdvcs.poiesis.machine.fluid.FluidContainerInteraction;
import org.kdvcs.poiesis.screen.AdsorptionColumnMenu;

import java.util.function.Supplier;

public record AdsorptionColumnFluidInteractPacket(BlockPos pos, boolean output) {
    public static void encode(
            AdsorptionColumnFluidInteractPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.output);
    }

    public static AdsorptionColumnFluidInteractPacket decode(FriendlyByteBuf buffer) {
        return new AdsorptionColumnFluidInteractPacket(
                buffer.readBlockPos(),
                buffer.readBoolean()
        );
    }

    public static void handle(
            AdsorptionColumnFluidInteractPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null
                    || !(player.containerMenu instanceof AdsorptionColumnMenu menu)
                    || !menu.machinePos().equals(packet.pos)
                    || player.distanceToSqr(
                    packet.pos.getX() + 0.5D,
                    packet.pos.getY() + 0.5D,
                    packet.pos.getZ() + 0.5D
            ) > 64.0D
                    || !(player.level().getBlockEntity(packet.pos)
                    instanceof AdsorptionColumnControllerBlockEntity controller)
                    || !controller.isFormed()) {
                return;
            }

            FluidContainerInteraction.interact(
                    player,
                    menu,
                    packet.output ? controller.cleanOutputTank() : controller.dirtyInputTank(),
                    !packet.output,
                    packet.output
            );
        });
        context.setPacketHandled(true);
    }
}
